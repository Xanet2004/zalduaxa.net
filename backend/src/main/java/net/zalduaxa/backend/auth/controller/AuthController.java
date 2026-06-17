package net.zalduaxa.backend.auth.controller;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import net.zalduaxa.backend.auth.dto.request.LoginRequest;
import net.zalduaxa.backend.auth.dto.request.SignupRequest;
import net.zalduaxa.backend.auth.dto.response.AuthUserResponse;
import net.zalduaxa.backend.common.dto.MessageResponse;
import net.zalduaxa.backend.auth.dto.response.UserResponse;
import net.zalduaxa.backend.auth.model.User;
import net.zalduaxa.backend.auth.security.AuthenticatedUser;
import net.zalduaxa.backend.auth.service.AuthService;
import net.zalduaxa.backend.auth.service.CurrentUserService;
import net.zalduaxa.backend.auth.service.LoginSession;
import net.zalduaxa.backend.auth.service.SessionService;

@RestController
@RequestMapping("/auth")
@CrossOrigin(
    origins = "${app.cors.origin}",
    allowCredentials = "true",
    maxAge = 3600
)
@Tag(name = "Authentication", description = "Login, signup, logout and session endpoints")
public class AuthController {

    private final AuthService authService;
    private final SessionService sessionService;
    private final CurrentUserService currentUserService;

    @Value("${app.auth.cookie.name:token}")
    private String cookieName;

    @Value("${app.auth.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.auth.cookie.sameSite:Lax}")
    private String cookieSameSite;

    @Value("${app.auth.cookie.domain:}")
    private String cookieDomain;

    @Value("${app.auth.cookie.path:/}")
    private String cookiePath;

    @Value("${app.auth.cookie.maxAgeDays:1}")
    private long cookieMaxAgeDays;

    public AuthController(
            AuthService authService,
            SessionService sessionService,
            CurrentUserService currentUserService) {
        this.authService = authService;
        this.sessionService = sessionService;
        this.currentUserService = currentUserService;
    }

    @PostMapping(value = "/signup", consumes = "application/json")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req) {
        authService.register(req);
        return ResponseEntity.ok(new MessageResponse("User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req, HttpServletResponse response) {
        LoginSession loginSession = authService.loginAndCreateSession(req);

        response.addHeader(HttpHeaders.SET_COOKIE, buildAuthCookie(loginSession.token()).toString());
        return ResponseEntity.ok(new AuthUserResponse(new UserResponse(loginSession.user())));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        sessionService.logoutByRequest(request);
        SecurityContextHolder.clearContext();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteAuthCookie().toString());
        return ResponseEntity.ok(new MessageResponse("Logged out"));
    }

    @GetMapping("/session")
    public ResponseEntity<?> getSession(@AuthenticationPrincipal AuthenticatedUser principal) {
        User user = currentUserService.loadUser(principal);

        return ResponseEntity.ok(new AuthUserResponse(new UserResponse(user)));
    }

    private ResponseCookie buildAuthCookie(String token) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(cookieName, token)
            .httpOnly(true) // TODO: false when building the final project
            .secure(cookieSecure)
            .path(cookiePath)
            .maxAge(Duration.ofDays(cookieMaxAgeDays))
            .sameSite(cookieSameSite);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            b.domain(cookieDomain);
        }

        return b.build();
    }

    private ResponseCookie deleteAuthCookie() {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(cookieName, "")
            .httpOnly(true) // TODO: false when building the final project
            .secure(cookieSecure)
            .path(cookiePath)
            .maxAge(Duration.ZERO)
            .sameSite(cookieSameSite);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            b.domain(cookieDomain);
        }

        return b.build();
    }
}