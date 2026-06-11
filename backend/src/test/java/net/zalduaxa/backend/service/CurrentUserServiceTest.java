package net.zalduaxa.backend.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.zalduaxa.backend.exception.UnauthorizedException;
import net.zalduaxa.backend.model.user.User;
import net.zalduaxa.backend.model.user.UserRepository;
import net.zalduaxa.backend.security.AuthenticatedUser;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void loadUser_existingUser_returnsUser() {
        CurrentUserService service = service();
        AuthenticatedUser principal = principal();
        User user = user();

        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        User result = service.loadUser(principal);

        assertSame(user, result);
    }

    @Test
    void loadUser_nullPrincipal_throwsUnauthorizedException() {
        CurrentUserService service = service();

        assertThrows(UnauthorizedException.class, () -> service.loadUser(null));
    }

    @Test
    void loadUser_nullPrincipalId_throwsUnauthorizedException() {
        CurrentUserService service = service();
        AuthenticatedUser principal = new AuthenticatedUser(null, "xanet", "xanet@example.com", "guest");

        assertThrows(UnauthorizedException.class, () -> service.loadUser(principal));
    }

    @Test
    void loadUser_missingUser_throwsUnauthorizedException() {
        CurrentUserService service = service();
        AuthenticatedUser principal = principal();

        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> service.loadUser(principal));
    }

    private CurrentUserService service() {
        return new CurrentUserService(userRepository);
    }

    private AuthenticatedUser principal() {
        return new AuthenticatedUser(1, "xanet", "xanet@example.com", "guest");
    }

    private User user() {
        User user = new User();
        user.setId(1);
        user.setUsername("xanet");
        user.setEmail("xanet@example.com");
        return user;
    }
}