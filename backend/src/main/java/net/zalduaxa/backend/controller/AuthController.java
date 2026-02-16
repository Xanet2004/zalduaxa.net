package net.zalduaxa.backend.controller;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.zalduaxa.backend.model.requestUser.RequestUser;
import net.zalduaxa.backend.model.responseUser.ResponseUser;
import net.zalduaxa.backend.model.user.User;
import net.zalduaxa.backend.service.AuthService;

@RestController
@RequestMapping("/auth")
@CrossOrigin(
    origins = "http://localhost:5173",
    allowCredentials = "true",
    maxAge = 3600
)
public class AuthController {

    private final AuthService authService;

    @Value("${app.auth.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.auth.cookie.sameSite:Lax}")
    private String cookieSameSite;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(value = "/signup", consumes = "application/json")
    public ResponseEntity<?> signup(@RequestBody RequestUser req) {
        authService.register(req);
        return ResponseEntity.ok(new MessageResponse("User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody RequestUser req, HttpServletResponse response) {
        User user = authService.loginAndCreateSession(req);

        String token = authService.issueJwt(user);

        response.addHeader(HttpHeaders.SET_COOKIE, buildAuthCookie(token).toString());
        return ResponseEntity.ok(new UserResponse(new ResponseUser(user)));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logoutByRequest(request);

        response.addHeader(HttpHeaders.SET_COOKIE, deleteAuthCookie().toString());
        return ResponseEntity.ok(new MessageResponse("Logged out"));
    }

    @GetMapping("/session")
    public ResponseEntity<?> getSession(HttpServletRequest request) {
        User user = authService.getUserFromRequest(request);
        authService.assertHasActiveSession(user.getId());

        return ResponseEntity.ok(new UserResponse(new ResponseUser(user)));
    }

    private ResponseCookie buildAuthCookie(String token) {
        return ResponseCookie.from("token", token)
            .httpOnly(true)
            .secure(cookieSecure)
            .path("/")
            .maxAge(Duration.ofDays(1))
            .sameSite(cookieSameSite)
            .build();
    }

    private ResponseCookie deleteAuthCookie() {
        return ResponseCookie.from("token", "")
            .httpOnly(true)
            .secure(cookieSecure)
            .path("/")
            .maxAge(Duration.ZERO)
            .sameSite(cookieSameSite)
            .build();
    }

    // simple response DTOs
    public record MessageResponse(String message) {}
    public record UserResponse(ResponseUser user) {}
}
