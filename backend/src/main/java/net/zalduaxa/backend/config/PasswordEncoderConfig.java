package net.zalduaxa.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import net.zalduaxa.backend.auth.security.PepperedPasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder(@Value("${app.password.pepper}") String pepper) {
        return new PepperedPasswordEncoder(new BCryptPasswordEncoder(), pepper);
    }
}