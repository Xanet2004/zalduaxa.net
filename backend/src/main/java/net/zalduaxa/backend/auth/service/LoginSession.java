package net.zalduaxa.backend.service;

import net.zalduaxa.backend.model.user.User;

public record LoginSession(User user, String token) {}