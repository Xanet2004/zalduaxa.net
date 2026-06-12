package net.zalduaxa.backend.auth.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpServletRequest;
import net.zalduaxa.backend.auth.dto.request.LoginRequest;
import net.zalduaxa.backend.auth.dto.request.SignupRequest;
import net.zalduaxa.backend.common.exception.ApiExceptionHandler;
import net.zalduaxa.backend.common.exception.BadRequestException;
import net.zalduaxa.backend.common.exception.UnauthorizedException;
import net.zalduaxa.backend.auth.model.Role;
import net.zalduaxa.backend.auth.model.User;
import net.zalduaxa.backend.auth.security.AuthenticatedUser;
import net.zalduaxa.backend.auth.security.JwtAuthenticationFilter;
import net.zalduaxa.backend.auth.service.AuthService;
import net.zalduaxa.backend.auth.service.CurrentUserService;
import net.zalduaxa.backend.auth.service.LoginSession;
import net.zalduaxa.backend.auth.service.SessionService;

@WebMvcTest(
    controllers = AuthController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
@TestPropertySource(properties = {
    "app.auth.cookie.name=test-cookie",
    "app.auth.cookie.secure=false",
    "app.auth.cookie.sameSite=Lax",
    "app.auth.cookie.path=/",
    "app.auth.cookie.maxAgeDays=1",
    "app.auth.cookie.domain="
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private SessionService sessionService;

    @MockitoBean
    private CurrentUserService currentUserService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void signup_validRequest_returnsOk() throws Exception {
        when(authService.register(any(SignupRequest.class))).thenReturn(user("xanet", "guest"));

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "xanet",
                      "email": "xanet@example.com",
                      "password": "Password123!",
                      "repeated_password": "Password123!"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    void signup_whenServiceThrowsBadRequest_returnsBadRequest() throws Exception {
        when(authService.register(any(SignupRequest.class)))
            .thenThrow(new BadRequestException("Username already exists"));

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "xanet",
                      "email": "xanet@example.com",
                      "password": "Password123!",
                      "repeated_password": "Password123!"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    void login_validCredentials_returnsOkAndAuthCookie() throws Exception {
        User user = user("xanet", "guest");

        when(authService.loginAndCreateSession(any(LoginRequest.class)))
                .thenReturn(new LoginSession(user, "jwt-token"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                    "username": "xanet",
                    "password": "Password123!"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("test-cookie=jwt-token")))
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Path=/")))
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=86400")))
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Lax")));
    }

    @Test
    void login_invalidCredentials_returnsUnauthorized() throws Exception {
        when(authService.loginAndCreateSession(any(LoginRequest.class)))
            .thenThrow(new UnauthorizedException("Invalid username or password"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "xanet",
                      "password": "WrongPassword123!"
                    }
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void session_authenticated_returnsOk() throws Exception {
        User user = user("xanet", "guest");

        authenticateAs(new AuthenticatedUser(1, "xanet", "xanet@example.com", "guest"));
        when(currentUserService.loadUser(any())).thenReturn(user);

        mockMvc.perform(get("/auth/session"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user").exists());
    }

    @Test
    void session_unauthenticated_returnsUnauthorized() throws Exception {
        when(currentUserService.loadUser(null))
            .thenThrow(new UnauthorizedException("Missing auth token"));

        mockMvc.perform(get("/auth/session"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value("Missing auth token"));
    }

    @Test
    void logout_authenticated_returnsOkAndExpiredCookie() throws Exception {
        doNothing().when(sessionService).logoutByRequest(any(HttpServletRequest.class));

        mockMvc.perform(post("/auth/logout")
                .cookie(new jakarta.servlet.http.Cookie("test-cookie", "jwt-token")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Logged out"))
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("test-cookie=")))
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")))
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Path=/")))
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Lax")));
    }

    @Test
    void logout_whenServiceThrowsBadRequest_returnsBadRequest() throws Exception {
        doThrow(new BadRequestException("User is not in session"))
            .when(sessionService).logoutByRequest(any(HttpServletRequest.class));

        mockMvc.perform(post("/auth/logout"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("User is not in session"));
    }

    private void authenticateAs(AuthenticatedUser principal) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + principal.roleName().toUpperCase()))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private User user(String username, String roleName) {
        User user = new User();

        setId(user, 1);
        user.setUsername(username);
        user.setFullName(username + " Full Name");
        user.setEmail(username + "@example.com");
        user.setPasswordHash("hashed-password");
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