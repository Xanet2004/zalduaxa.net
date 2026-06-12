package net.zalduaxa.backend.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.crypto.password.PasswordEncoder;

public final class PepperedPasswordEncoder implements PasswordEncoder {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final PasswordEncoder delegate;
    private final byte[] pepperBytes;

    public PepperedPasswordEncoder(PasswordEncoder delegate, String pepper) {
        this.delegate = Objects.requireNonNull(delegate, "PasswordEncoder delegate is required");

        if (pepper == null || pepper.isBlank()) {
            throw new IllegalStateException("Password pepper must be configured");
        }

        this.pepperBytes = pepper.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return delegate.encode(applyPepper(rawPassword));
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            return false;
        }

        return delegate.matches(applyPepper(rawPassword), encodedPassword);
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return delegate.upgradeEncoding(encodedPassword);
    }

    private String applyPepper(CharSequence rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("Raw password is required");
        }

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(pepperBytes, HMAC_ALGORITHM);
            mac.init(secretKey);

            byte[] hmac = mac.doFinal(rawPassword.toString().getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hmac);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to apply password pepper", e);
        }
    }
}