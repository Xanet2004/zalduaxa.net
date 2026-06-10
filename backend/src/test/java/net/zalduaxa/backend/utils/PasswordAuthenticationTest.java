package net.zalduaxa.backend.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordAuthenticationTest {

    private final PasswordAuthentication passwordAuthentication = new PasswordAuthentication();

    @Test
    void hash_returnsDifferentOutputForSamePasswordBecauseOfSalt() {
        String password = "StrongPassword123!";

        String firstHash = passwordAuthentication.hash(password.toCharArray());
        String secondHash = passwordAuthentication.hash(password.toCharArray());

        assertNotEquals(firstHash, secondHash);
    }

    @Test
    void hash_outputMatchesExpectedFormat() {
        String hash = passwordAuthentication.hash("StrongPassword123!".toCharArray());

        assertTrue(hash.matches("^\\$31\\$\\d{1,2}\\$.{43}$"));
    }

    @Test
    void authenticate_returnsTrueForCorrectPassword() {
        String password = "StrongPassword123!";
        String hash = passwordAuthentication.hash(password.toCharArray());

        boolean result = passwordAuthentication.authenticate(password.toCharArray(), hash);

        assertTrue(result);
    }

    @Test
    void authenticate_returnsFalseForWrongPassword() {
        String correctPassword = "StrongPassword123!";
        String wrongPassword = "WrongPassword123!";
        String hash = passwordAuthentication.hash(correctPassword.toCharArray());

        boolean result = passwordAuthentication.authenticate(wrongPassword.toCharArray(), hash);

        assertFalse(result);
    }

    @Test
    void authenticate_throwsWhenTokenFormatIsInvalid() {
        assertThrows(
            IllegalArgumentException.class,
            () -> passwordAuthentication.authenticate("password".toCharArray(), "invalid-token")
        );
    }

    @SuppressWarnings("deprecation")
    @Test
    void deprecatedStringMethods_stillWork() {
        String password = "StrongPassword123!";
        String hash = passwordAuthentication.hash(password);

        assertTrue(passwordAuthentication.authenticate(password, hash));
    }
}