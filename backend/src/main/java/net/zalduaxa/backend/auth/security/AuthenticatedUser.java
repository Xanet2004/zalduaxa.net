package net.zalduaxa.backend.auth.security;

public record AuthenticatedUser(
        Integer id,
        String username,
        String email,
        String roleName
) {}