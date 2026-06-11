package net.zalduaxa.backend.security;

public record AuthenticatedUser(
        Integer id,
        String username,
        String email,
        String roleName
) {}