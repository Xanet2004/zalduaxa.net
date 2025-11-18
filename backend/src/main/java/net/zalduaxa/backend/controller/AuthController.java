package net.zalduaxa.backend.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.zalduaxa.backend.model.requestUser.RequestUser;
import net.zalduaxa.backend.model.responseUser.ResponseUser;
import net.zalduaxa.backend.model.session.Session;
import net.zalduaxa.backend.model.session.SessionRepository;
import net.zalduaxa.backend.model.user.User;
import net.zalduaxa.backend.service.AuthService;
import net.zalduaxa.backend.service.JwtService;

@CrossOrigin(
    origins = "http://localhost:5173",
    allowCredentials = "true",
    maxAge = 3600
)
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SessionRepository sessionRepository;

    @PostMapping(value = "/signup", consumes = "application/json")
    public ResponseEntity<?> signup(@RequestBody RequestUser req) {
        try {
            User user = authService.register(req);
            return ResponseEntity.ok(Map.of("message", "User registered successfully"));

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

            // ? Check user has a session
            if (sessionRepository.findByUserId(user.getId().longValue()).isPresent()) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "User already is in session"));
            }

            String token = jwtService.generateToken(user.getUsername());

            ResponseCookie cookie = ResponseCookie.from("token", token)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(24 * 60 * 60)
                    .sameSite("Strict")
                    .build();

            response.addHeader("Set-Cookie", cookie.toString());

            // ? Add user and token to session
            Session session = new Session(user.getId(), token);
            sessionRepository.save(session);

            return ResponseEntity.ok(Map.of("user", new ResponseUser(user)));

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response, HttpServletRequest request) {
        try {
            User user = authService.getUserFromToken(extractToken(request), jwtService);
            Optional<Session> sessionOpt = sessionRepository.findByUserId(user.getId().longValue());
            // ? Check user has a session
            if(!sessionRepository.existsById(sessionOpt.get().getId())) return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "User is not in session"));

            //? Remove token
            ResponseCookie deleteCookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .build();

            response.addHeader("Set-Cookie", deleteCookie.toString());

            //? Remove user and token from session
            if (sessionOpt.isPresent()) {
                sessionRepository.delete(sessionOpt.get());
            }

            return ResponseEntity.ok(Map.of("message", "User is not longer in session"));

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/session")
    public ResponseEntity<?> getSession(HttpServletResponse response, HttpServletRequest request) {
        try {
            User user = authService.getUserFromToken(extractToken(request), jwtService);
            Optional<Session> sessionOpt = sessionRepository.findByUserId(user.getId().longValue());
            // ? Check user has a session
            if(!sessionRepository.existsById(sessionOpt.get().getId())) return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "User is not in session"));

            return ResponseEntity.ok(Map.of("user", new ResponseUser(user)));

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("token")) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
