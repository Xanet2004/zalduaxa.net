package net.zalduaxa.backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

class JwtServiceTest {

    private static final String TEST_SECRET =
        "dGhpcyBpcyBhIHRlc3Qgc2VjcmV0IGZvciBqd3QgdGVzdGluZyBwdXJwb3Nlcw==";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // These fields exist after Fix 2.
        // If your JwtService still has the old hardcoded SECRET field,
        // complete Fix 2 first.
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 86_400_000L);
    }

    @Test
    void generateToken_returnsNonBlankJwtWithThreeSegments() {
        String token = jwtService.generateToken("xanet");

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void getUsername_returnsCorrectSubject() {
        String token = jwtService.generateToken("xanet");

        String username = jwtService.getUsername(token);

        assertEquals("xanet", username);
    }

    @Test
    void validateToken_returnsTrueForValidToken() {
        String token = jwtService.generateToken("xanet");

        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void validateToken_returnsFalseForTamperedToken() {
        String token = jwtService.generateToken("xanet");
        String tamperedToken = token.substring(0, token.length() - 2) + "xx";

        assertFalse(jwtService.validateToken(tamperedToken));
    }

    @Test
    void validateToken_returnsFalseForExpiredToken() {
        String expiredToken = Jwts.builder()
            .setSubject("xanet")
            .setIssuedAt(new Date(System.currentTimeMillis() - 172_800_000L))
            .setExpiration(new Date(System.currentTimeMillis() - 86_400_000L))
            .signWith(SignatureAlgorithm.HS256, TEST_SECRET)
            .compact();

        assertFalse(jwtService.validateToken(expiredToken));
    }

    @Test
    void validateToken_returnsFalseForNullToken() {
        assertDoesNotThrow(() -> {
            boolean result = jwtService.validateToken(null);
            assertFalse(result);
        });
    }

    @Test
    void validateToken_returnsFalseForBlankToken() {
        assertDoesNotThrow(() -> {
            boolean result = jwtService.validateToken("");
            assertFalse(result);
        });
    }
}