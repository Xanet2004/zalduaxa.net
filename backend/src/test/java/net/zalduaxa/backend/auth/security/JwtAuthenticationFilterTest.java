package net.zalduaxa.backend.auth.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Optional;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import net.zalduaxa.backend.auth.model.Role;
import net.zalduaxa.backend.auth.model.User;
import net.zalduaxa.backend.auth.model.UserRepository;
import net.zalduaxa.backend.auth.service.JwtService;
import net.zalduaxa.backend.auth.service.SessionService;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String TOKEN = "valid.jwt.token";
    private static final String USERNAME = "admin";

    @Mock
    private SessionService sessionService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        filter = new JwtAuthenticationFilter(sessionService, jwtService, userRepository);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_whenTokenIsNull_continuesWithoutAuthentication() throws Exception {
        when(sessionService.extractToken(request)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).validateToken(anyString());
        verify(jwtService, never()).getUsername(anyString());
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void doFilterInternal_whenTokenIsBlank_continuesWithoutAuthentication() throws Exception {
        when(sessionService.extractToken(request)).thenReturn("   ");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).validateToken(anyString());
        verify(jwtService, never()).getUsername(anyString());
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void doFilterInternal_whenTokenIsInvalid_clearsContextAndContinuesWithoutAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(existingAuthentication());

        when(sessionService.extractToken(request)).thenReturn(TOKEN);
        when(jwtService.validateToken(TOKEN)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
        verify(jwtService).validateToken(TOKEN);
        verify(jwtService, never()).getUsername(anyString());
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void doFilterInternal_whenUserDoesNotExist_clearsContextAndContinuesWithoutAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(existingAuthentication());

        when(sessionService.extractToken(request)).thenReturn(TOKEN);
        when(jwtService.validateToken(TOKEN)).thenReturn(true);
        when(jwtService.getUsername(TOKEN)).thenReturn(USERNAME);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
        verify(sessionService, never()).hasActiveSession(null);
    }

    @Test
    void doFilterInternal_whenUserHasNoActiveSession_clearsContextAndContinuesWithoutAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(existingAuthentication());

        User user = user(1, USERNAME, "admin");

        when(sessionService.extractToken(request)).thenReturn(TOKEN);
        when(jwtService.validateToken(TOKEN)).thenReturn(true);
        when(jwtService.getUsername(TOKEN)).thenReturn(USERNAME);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(sessionService.hasActiveSession(1)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenTokenUserAndSessionAreValid_authenticatesUserWithRoleAuthority() throws Exception {
        User user = user(1, USERNAME, "admin");

        when(sessionService.extractToken(request)).thenReturn(TOKEN);
        when(jwtService.validateToken(TOKEN)).thenReturn(true);
        when(jwtService.getUsername(TOKEN)).thenReturn(USERNAME);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(sessionService.hasActiveSession(1)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertInstanceOf(UsernamePasswordAuthenticationToken.class, authentication);
        assertTrue(authentication.isAuthenticated());

        AuthenticatedUser principal = assertInstanceOf(AuthenticatedUser.class, authentication.getPrincipal());
        assertEquals(1, principal.id());
        assertEquals(USERNAME, principal.username());
        assertEquals("admin@example.com", principal.email());
        assertEquals("admin", principal.roleName());

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream().anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())));

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenUserHasNullRole_authenticatesWithoutAuthorities() throws Exception {
        User user = user(1, USERNAME, null);

        when(sessionService.extractToken(request)).thenReturn(TOKEN);
        when(jwtService.validateToken(TOKEN)).thenReturn(true);
        when(jwtService.getUsername(TOKEN)).thenReturn(USERNAME);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(sessionService.hasActiveSession(1)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertInstanceOf(UsernamePasswordAuthenticationToken.class, authentication);
        assertTrue(authentication.isAuthenticated());

        AuthenticatedUser principal = assertInstanceOf(AuthenticatedUser.class, authentication.getPrincipal());
        assertEquals(1, principal.id());
        assertEquals(USERNAME, principal.username());
        assertEquals("admin@example.com", principal.email());
        assertNull(principal.roleName());

        assertTrue(authentication.getAuthorities().isEmpty());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenUserRoleNameIsBlank_authenticatesWithoutAuthorities() throws Exception {
        User user = user(1, USERNAME, "   ");

        when(sessionService.extractToken(request)).thenReturn(TOKEN);
        when(jwtService.validateToken(TOKEN)).thenReturn(true);
        when(jwtService.getUsername(TOKEN)).thenReturn(USERNAME);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(sessionService.hasActiveSession(1)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertInstanceOf(UsernamePasswordAuthenticationToken.class, authentication);
        assertTrue(authentication.isAuthenticated());
        assertTrue(authentication.getAuthorities().isEmpty());

        AuthenticatedUser principal = assertInstanceOf(AuthenticatedUser.class, authentication.getPrincipal());
        assertEquals("   ", principal.roleName());

        verify(filterChain).doFilter(request, response);
    }

    private Authentication existingAuthentication() {
        return new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(99, "previous", "previous@example.com", "admin"),
                null
        );
    }

    private User user(Integer id, String username, String roleName) {
        User user = new User();

        ReflectionTestUtils.setField(user, "id", id);
        user.setUsername(username);
        user.setFullName(username + " Full Name");
        user.setEmail(username + "@example.com");
        user.setPasswordHash("hashed-password");

        if (roleName != null) {
            user.setRole(role(roleName));
        }

        return user;
    }

    private Role role(String name) {
        Role role = new Role();
        ReflectionTestUtils.setField(role, "name", name);
        return role;
    }
}