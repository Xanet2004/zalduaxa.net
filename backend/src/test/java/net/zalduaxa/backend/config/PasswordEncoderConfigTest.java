package net.zalduaxa.backend.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordEncoderConfigTest {

    private final PasswordEncoder passwordEncoder = new PasswordEncoderConfig().passwordEncoder();

    @Test
    void passwordEncoder_encode_returnsBCryptHash() {
        String hash = passwordEncoder.encode("Password123!");

        assertTrue(hash.startsWith("$2"));
        assertTrue(passwordEncoder.matches("Password123!", hash));
    }

    @Test
    void passwordEncoder_matches_returnsFalseForWrongPassword() {
        String hash = passwordEncoder.encode("Password123!");

        assertFalse(passwordEncoder.matches("WrongPassword123!", hash));
    }
}