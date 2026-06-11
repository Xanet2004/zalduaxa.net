package net.zalduaxa.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.http.HttpServletRequest;
import net.zalduaxa.backend.exception.BadRequestException;
import net.zalduaxa.backend.exception.UnauthorizedException;
import net.zalduaxa.backend.dto.request.LoginRequest;
import net.zalduaxa.backend.dto.request.SignupRequest;
import net.zalduaxa.backend.model.role.Role;
import net.zalduaxa.backend.model.role.RoleRepository;
import net.zalduaxa.backend.model.user.User;
import net.zalduaxa.backend.model.user.UserRepository;
import net.zalduaxa.backend.utils.PasswordAuthentication;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private RoleRepository roleRepo;

    @Mock
    private SessionService sessionService;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    private final PasswordAuthentication passwordAuthentication = new PasswordAuthentication();

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepo, roleRepo, jwtService, sessionService);

        ReflectionTestUtils.setField(authService, "seedEnabled", false);
        ReflectionTestUtils.setField(authService, "adminUsername", "admin");
        ReflectionTestUtils.setField(authService, "adminPassword", "Admin123!");
        ReflectionTestUtils.setField(authService, "adminEmail", "admin@example.com");
        ReflectionTestUtils.setField(authService, "guestUsername", "guest");
        ReflectionTestUtils.setField(authService, "guestPassword", "Guest123!");
        ReflectionTestUtils.setField(authService, "guestEmail", "guest@example.com");
    }

    @Test
    void register_success_assignsGuestRoleAndPasswordHash() {
        SignupRequest req = signupRequest("newuser", "new@example.com", "Password123!", "Password123!");
        Role guestRole = role("guest");

        when(userRepo.existsByUsername("newuser")).thenReturn(false);
        when(userRepo.existsByEmail("new@example.com")).thenReturn(false);
        when(roleRepo.findByName("guest")).thenReturn(guestRole);
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.register(req);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        assertNotNull(result.getPasswordHash());
        assertNotNull(result.getRole());
        assertEquals("guest", result.getRole().getName());
    }

    @Test
    void register_missingUsername_throwsBadRequestException() {
        SignupRequest req = signupRequest(null, "new@example.com", "Password123!", "Password123!");

        assertThrows(BadRequestException.class, () -> authService.register(req));
    }

    @Test
    void register_missingEmail_throwsBadRequestException() {
        SignupRequest req = signupRequest("newuser", null, "Password123!", "Password123!");

        assertThrows(BadRequestException.class, () -> authService.register(req));
    }

    @Test
    void register_missingPassword_throwsBadRequestException() {
        SignupRequest req = signupRequest("newuser", "new@example.com", null, null);

        assertThrows(BadRequestException.class, () -> authService.register(req));
    }

    @Test
    void register_duplicateUsername_throwsBadRequestException() {
        SignupRequest req = signupRequest("newuser", "new@example.com", "Password123!", "Password123!");

        when(userRepo.existsByUsername("newuser")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(req));
    }

    @Test
    void register_duplicateEmail_throwsBadRequestException() {
        SignupRequest req = signupRequest("newuser", "new@example.com", "Password123!", "Password123!");

        when(userRepo.existsByUsername("newuser")).thenReturn(false);
        when(userRepo.existsByEmail("new@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(req));
    }

    @Test
    void register_passwordMismatch_throwsBadRequestException() {
        SignupRequest req = signupRequest("newuser", "new@example.com", "Password123!", "Different123!");

        when(userRepo.existsByUsername("newuser")).thenReturn(false);
        when(userRepo.existsByEmail("new@example.com")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.register(req));
    }

    @Test
    void loginAndCreateSession_success_returnsLoginSessionAndCreatesSession() {
        User user = userWithPassword(1, "xanet", "Password123!", "guest");
        LoginRequest req = loginRequest("xanet", "Password123!");

        when(userRepo.findByUsername("xanet")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("xanet")).thenReturn("jwt-token");

        LoginSession result = authService.loginAndCreateSession(req);

        assertEquals(user, result.user());
        assertEquals("jwt-token", result.token());
        verify(sessionService).createSession(1, "jwt-token");
    }

    @Test
    void loginAndCreateSession_wrongUsername_throwsUnauthorizedException() {
        LoginRequest req = loginRequest("unknown", "Password123!");

        when(userRepo.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.loginAndCreateSession(req));
    }

    @Test
    void loginAndCreateSession_wrongPassword_throwsUnauthorizedException() {
        User user = userWithPassword(1, "xanet", "CorrectPassword123!", "guest");
        LoginRequest req = loginRequest("xanet", "WrongPassword123!");

        when(userRepo.findByUsername("xanet")).thenReturn(Optional.of(user));

        assertThrows(UnauthorizedException.class, () -> authService.loginAndCreateSession(req));
    }

    @Test
    void loginAndCreateSession_whenSessionServiceThrowsBadRequest_propagatesException() {
        User user = userWithPassword(1, "xanet", "Password123!", "guest");
        LoginRequest req = loginRequest("xanet", "Password123!");

        when(userRepo.findByUsername("xanet")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("xanet")).thenReturn("jwt-token");

        org.mockito.Mockito.doThrow(new BadRequestException("User already is in session"))
                .when(sessionService)
                .createSession(1, "jwt-token");

        assertThrows(BadRequestException.class, () -> authService.loginAndCreateSession(req));
    }

    @Test
    void getUserFromRequest_validToken_returnsUser() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = userWithPassword(1, "xanet", "Password123!", "guest");

        when(sessionService.extractToken(request)).thenReturn("jwt-token");
        when(jwtService.getUsername("jwt-token")).thenReturn("xanet");
        when(userRepo.findByUsername("xanet")).thenReturn(Optional.of(user));

        User result = authService.getUserFromRequest(request);

        assertEquals(user, result);
    }

    @Test
    void getUserFromRequest_missingToken_throwsUnauthorizedException() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(sessionService.extractToken(request)).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> authService.getUserFromRequest(request));
    }

    @Test
    void defaultUsers_seedDisabled_doesNotCreateUsers() {
        ReflectionTestUtils.setField(authService, "seedEnabled", false);

        ReflectionTestUtils.invokeMethod(authService, "defaultUsers");

        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void defaultUsers_seedEnabledAndEmptyDb_createsAdminAndGuest() {
        ReflectionTestUtils.setField(authService, "seedEnabled", true);

        when(userRepo.count()).thenReturn(0L);
        when(roleRepo.findByName("admin")).thenReturn(role("admin"));
        when(roleRepo.findByName("guest")).thenReturn(role("guest"));

        ReflectionTestUtils.invokeMethod(authService, "defaultUsers");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepo, times(2)).save(userCaptor.capture());

        assertEquals(2, userCaptor.getAllValues().size());
    }

    @Test
    void defaultUsers_seedEnabledButDbNotEmpty_doesNotCreateUsers() {
        ReflectionTestUtils.setField(authService, "seedEnabled", true);

        when(userRepo.count()).thenReturn(1L);

        ReflectionTestUtils.invokeMethod(authService, "defaultUsers");

        verify(userRepo, never()).save(any(User.class));
    }

    private SignupRequest signupRequest(String username, String email, String password, String repeatedPassword) {
        SignupRequest req = new SignupRequest();
        req.setUsername(username);
        req.setEmail(email);
        req.setPassword(password);
        req.setRepeatedPassword(repeatedPassword);
        return req;
    }

    private LoginRequest loginRequest(String username, String password) {
        LoginRequest req = new LoginRequest();
        req.setUsername(username);
        req.setPassword(password);
        return req;
    }

    private User userWithPassword(Number id, String username, String rawPassword, String roleName) {
        User user = new User();

        setId(user, id);
        user.setUsername(username);
        user.setFullName(username + " Full Name");
        user.setEmail(username + "@example.com");
        user.setPasswordHash(passwordAuthentication.hash(rawPassword.toCharArray()));
        user.setRole(role(roleName));

        return user;
    }

    private Role role(String name) {
        Role role = new Role();

        setId(role, "admin".equals(name) ? 1 : 2);
        ReflectionTestUtils.setField(role, "name", name);

        return role;
    }

    private void setId(Object target, Number id) {
        try {
            ReflectionTestUtils.setField(target, "id", id.intValue());
        } catch (IllegalArgumentException e) {
            ReflectionTestUtils.setField(target, "id", id.longValue());
        }
    }
}