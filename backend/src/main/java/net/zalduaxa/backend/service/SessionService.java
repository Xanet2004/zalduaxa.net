package net.zalduaxa.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import net.zalduaxa.backend.exception.BadRequestException;
import net.zalduaxa.backend.exception.UnauthorizedException;
import net.zalduaxa.backend.model.session.Session;
import net.zalduaxa.backend.model.session.SessionRepository;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;

    @Value("${app.auth.cookie.name:token}")
    private String cookieName;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    // TODO: Create require methods for more readibility
    public Session createSession(Integer userId, String token) {
        if (userId == null) {
            throw new UnauthorizedException("User id missing");
        }

        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Missing auth token");
        }

        if (sessionRepository.findByUserId(userId.longValue()).isPresent()) {
            throw new BadRequestException("User already is in session");
        }

        try {
            return sessionRepository.save(new Session(userId, token));
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("User already is in session");
        }
    }

    public void assertHasActiveSession(Number userId) {
        if (userId == null) {
            throw new UnauthorizedException("User id missing");
        }

        sessionRepository.findByUserId(userId.longValue())
                .orElseThrow(() -> new UnauthorizedException("User is not in session"));
    }

    public boolean hasActiveSession(Number userId) {
        if (userId == null) {
            return false;
        }

        return sessionRepository.findByUserId(userId.longValue()).isPresent();
    }

    public void logoutByRequest(HttpServletRequest request) {
        String token = extractToken(request);
        logoutByToken(token);
    }

    public void logoutByToken(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Missing auth token");
        }

        Session session = sessionRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("User is not in session"));

        sessionRepository.delete(session);
    }

    public String extractToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}