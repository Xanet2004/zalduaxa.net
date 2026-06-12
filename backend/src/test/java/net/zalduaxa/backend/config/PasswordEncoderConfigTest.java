package net.zalduaxa.backend.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordEncoderConfigTest {

    @Test
    void passwordEncoder_withPepper_returnsWorkingEncoder() {
        PasswordEncoderConfig config = new PasswordEncoderConfig();

        PasswordEncoder encoder = config.passwordEncoder("test-pepper-secret");

        String hash = encoder.encode("Password123!");

        assertTrue(hash.startsWith("$2"));
        assertTrue(encoder.matches("Password123!", hash));
        assertFalse(encoder.matches("WrongPassword123!", hash));
    }
}