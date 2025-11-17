package net.zalduaxa.backend.service;

import java.util.Date;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;

@Service
public class JwtService {

    // Clave secreta, debe ser larga y segura en producción
    private final String SECRET = "w1o5J5g/4yVYg+zV8Jd1yP4l+Fg4Z8YpQZ3ZPi5/ooQ=";

    // Generar token
    public String generateToken(String username) {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 día
            .signWith(SignatureAlgorithm.HS256, SECRET)
            .compact();
    }

    // Validar token
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Obtener username del token
    public String getUsername(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}