package net.zalduaxa.backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import net.zalduaxa.backend.model.requestUser.RequestUser;
import net.zalduaxa.backend.model.session.Session;
import net.zalduaxa.backend.model.session.SessionRepository;
import net.zalduaxa.backend.model.user.User;
import net.zalduaxa.backend.service.AuthService;
import net.zalduaxa.backend.service.JwtService;

@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SessionRepository sessionRepository;

    @PostMapping(value = "/register", consumes = "application/json")
    public ResponseEntity<?> register(@RequestBody RequestUser req) {
        try {
            User user = authService.register(req);
            return ResponseEntity.ok("User registered successfully");

        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody RequestUser req, HttpServletResponse response) {
        try {
            User user = authService.login(req);

            // TODO Check user has a session
            if(sessionRepository.existsById(user.getId())) return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "User already is in session"));
            else System.out.println("USER DOES NOT EXISTS ON SESSION");

            String token = jwtService.generateToken(user.getUsername());

            ResponseCookie cookie = ResponseCookie.from("token", token)
                    .httpOnly(true)
                    .secure(false) // cambiar a true en producción con HTTPS
                    .path("/")
                    .maxAge(24 * 60 * 60)
                    .sameSite("Strict")
                    .build();

            response.addHeader("Set-Cookie", cookie.toString());

            // TODO Add user and token to session
            Session session = new Session(user.getId(), token);
            sessionRepository.save(session);

            return ResponseEntity.ok(Map.of("user", user));

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(false) // cambiar a true en producción con HTTPS
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }
}
