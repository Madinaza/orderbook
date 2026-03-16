package com.aghaz.orderbook.trading.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

@Component
public class JwtTokenService {

    private final String secret;
    private Key key;

    public JwtTokenService(@Value("${app.security.jwt.secret}") String secret) {
        this.secret = secret;
    }

    @PostConstruct
    void init() {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes long.");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
    }

    public long readTraderId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    @SuppressWarnings("unchecked")
    public List<String> readRoles(String token) {
        Object roles = parseClaims(token).get("roles");
        if (roles instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}