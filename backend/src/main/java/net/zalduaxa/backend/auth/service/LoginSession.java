package net.zalduaxa.backend.auth.service;

import net.zalduaxa.backend.auth.model.User;

public record LoginSession(User user, String token) {}