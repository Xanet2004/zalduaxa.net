package net.zalduaxa.backend.service;

import org.springframework.stereotype.Service;

import net.zalduaxa.backend.exception.UnauthorizedException;
import net.zalduaxa.backend.model.user.User;
import net.zalduaxa.backend.model.user.UserRepository;
import net.zalduaxa.backend.security.AuthenticatedUser;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User loadUser(AuthenticatedUser principal) {
        if (principal == null || principal.id() == null) {
            throw new UnauthorizedException("Missing auth token");
        }

        return userRepository.findById(principal.id())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }
}