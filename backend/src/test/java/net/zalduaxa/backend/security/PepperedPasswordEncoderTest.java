package net.zalduaxa.backend.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class PepperedPasswordEncoderTest {

    private static final String PEPPER = "test-pepper-secret";
    private static final String OTHER_PEPPER = "other-test-pepper-secret";
    private static final String PASSWORD = "Password123!";

    @Test
    void encode_returnsBCryptHash() {
        PasswordEncoder encoder = encoderWithPepper(PEPPER);

        String hash = encoder.encode(PASSWORD);

        assertTrue(hash.startsWith("$2"));
        assertTrue(encoder.matches(PASSWORD, hash));
    }

    @Test
    void matches_returnsFalseForWrongPassword() {
        PasswordEncoder encoder = encoderWithPepper(PEPPER);
        String hash = encoder.encode(PASSWORD);

        assertFalse(encoder.matches("WrongPassword123!", hash));
    }

    @Test
    void samePasswordProducesDifferentHashesBecauseBCryptSaltIsAutomatic() {
        PasswordEncoder encoder = encoderWithPepper(PEPPER);

        String firstHash = encoder.encode(PASSWORD);
        String secondHash = encoder.encode(PASSWORD);

        assertNotEquals(firstHash, secondHash);
        assertTrue(encoder.matches(PASSWORD, firstHash));
        assertTrue(encoder.matches(PASSWORD, secondHash));
    }

    @Test
    void differentPepperDoesNotMatch() {
        PasswordEncoder encoder = encoderWithPepper(PEPPER);
        PasswordEncoder encoderWithDifferentPepper = encoderWithPepper(OTHER_PEPPER);

        String hash = encoder.encode(PASSWORD);

        assertTrue(encoder.matches(PASSWORD, hash));
        assertFalse(encoderWithDifferentPepper.matches(PASSWORD, hash));
    }

    @Test
    void nullPepper_throwsIllegalStateException() {
        assertThrows(
                IllegalStateException.class,
                () -> new PepperedPasswordEncoder(new BCryptPasswordEncoder(), null));
    }

    @Test
    void blankPepper_throwsIllegalStateException() {
        assertThrows(
                IllegalStateException.class,
                () -> new PepperedPasswordEncoder(new BCryptPasswordEncoder(), " "));
    }

    @Test
    void nullDelegate_throwsNullPointerException() {
        assertThrows(
                NullPointerException.class,
                () -> new PepperedPasswordEncoder(null, PEPPER));
    }

    @Test
    void encode_nullRawPassword_throwsIllegalArgumentException() {
        PasswordEncoder encoder = encoderWithPepper(PEPPER);

        assertThrows(
                IllegalArgumentException.class,
                () -> encoder.encode(null));
    }

    @Test
    void matches_nullRawPassword_throwsIllegalArgumentException() {
        PasswordEncoder encoder = encoderWithPepper(PEPPER);
        String hash = encoder.encode(PASSWORD);

        assertThrows(
                IllegalArgumentException.class,
                () -> encoder.matches(null, hash));
    }

    @Test
    void matches_nullHash_returnsFalse() {
        PasswordEncoder encoder = encoderWithPepper(PEPPER);

        assertFalse(encoder.matches(PASSWORD, null));
    }

    @Test
    void matches_blankHash_returnsFalse() {
        PasswordEncoder encoder = encoderWithPepper(PEPPER);

        assertFalse(encoder.matches(PASSWORD, " "));
    }

    private PasswordEncoder encoderWithPepper(String pepper) {
        return new PepperedPasswordEncoder(new BCryptPasswordEncoder(), pepper);
    }
}