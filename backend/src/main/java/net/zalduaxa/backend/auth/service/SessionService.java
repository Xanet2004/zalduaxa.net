package net.zalduaxa.backend.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import net.zalduaxa.backend.common.exception.BadRequestException;
import net.zalduaxa.backend.common.exception.UnauthorizedException;
import net.zalduaxa.backend.auth.model.Session;
import net.zalduaxa.backend.auth.model.SessionRepository;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;

    @Value("${app.auth.cookie.name:token}")
    private String cookieName;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public Session createSession(Integer userId, String token) {
        if (userId == null) {
            throw new UnauthorizedException("User id missing");
        }

        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Missing auth token");
        }

        sessionRepository.deleteByUserId(userId);

        try {
            return sessionRepository.save(new Session(userId, token));
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Could not create session");
        }
    }

    public void assertHasActiveSession(Number userId) {
        Integer cleanUserId = requireUserId(userId);

        sessionRepository.findByUserId(cleanUserId)
                .orElseThrow(() -> new UnauthorizedException("User is not in session"));
    }

    public boolean hasActiveSession(Number userId) {
        if (userId == null) {
            return false;
        }

        return sessionRepository.findByUserId(userId.intValue()).isPresent();
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

    private Integer requireUserId(Number userId) {
        if (userId == null) {
            throw new UnauthorizedException("User id missing");
        }

        return userId.intValue();
    }
}