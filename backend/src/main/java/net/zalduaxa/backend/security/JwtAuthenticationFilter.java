package net.zalduaxa.backend.security;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.zalduaxa.backend.model.role.Role;
import net.zalduaxa.backend.model.user.User;
import net.zalduaxa.backend.model.user.UserRepository;
import net.zalduaxa.backend.service.JwtService;
import net.zalduaxa.backend.service.SessionService;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SessionService sessionService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            SessionService sessionService,
            JwtService jwtService,
            UserRepository userRepository) {
        this.sessionService = sessionService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = sessionService.extractToken(request);

        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!jwtService.validateToken(token)) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtService.getUsername(token);

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !sessionService.hasActiveSession(user.getId())) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        AuthenticatedUser principal = toPrincipal(user);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        toAuthorities(user)
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private AuthenticatedUser toPrincipal(User user) {
        Role role = user.getRole();
        String roleName = role != null ? role.getName() : null;

        return new AuthenticatedUser(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roleName
        );
    }

    private List<SimpleGrantedAuthority> toAuthorities(User user) {
        Role role = user.getRole();

        if (role == null || role.getName() == null || role.getName().isBlank()) {
            return List.of();
        }

        String authority = "ROLE_" + role.getName().toUpperCase(Locale.ROOT);
        return List.of(new SimpleGrantedAuthority(authority));
    }
}