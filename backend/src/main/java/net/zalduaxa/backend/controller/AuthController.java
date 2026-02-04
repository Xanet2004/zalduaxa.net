package net.zalduaxa.backend.controller;

import java.time.Duration;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.zalduaxa.backend.model.requestUser.RequestUser;
import net.zalduaxa.backend.model.responseUser.ResponseUser;
import net.zalduaxa.backend.model.user.User;
import net.zalduaxa.backend.service.AuthService;
import net.zalduaxa.backend.exception.ApiExceptionHandler;
import net.zalduaxa.backend.exception.UnauthorizedException;

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
        User user = authService.loginAndCreateSession(req); // move session logic into service

        String token = authService.issueJwt(user); // or return token from service

        response.addHeader(HttpHeaders.SET_COOKIE, buildAuthCookie(token).toString());
        return ResponseEntity.ok(new UserResponse(new ResponseUser(user)));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = extractToken(request);
        authService.logoutByToken(token);

        response.addHeader(HttpHeaders.SET_COOKIE, deleteAuthCookie().toString());
        return ResponseEntity.ok(new MessageResponse("Logged out"));
    }

    @GetMapping("/session")
    public ResponseEntity<?> getSession(HttpServletRequest request) {
        String token = extractToken(request);
        User user = authService.getUserFromToken(token);
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

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                .filter(c -> "token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("Missing auth token"));
        }

        throw new UnauthorizedException("Missing auth token");
    }


    // simple response DTOs
    public record MessageResponse(String message) {}
    public record UserResponse(ResponseUser user) {}
}
