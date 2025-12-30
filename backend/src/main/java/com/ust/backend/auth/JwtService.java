package com.ust.backend.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${ust.jwt.secret:change-this-dev-secret-change-this-dev-secret}")
    private String secret;

    @Value("${ust.jwt.expiration-seconds:3600}")
    private long expirationSeconds;

    private SecretKey key;

    @PostConstruct
    void init() {
        // Ensure at least 32 bytes for HS256
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            // pad to 32 bytes if short (dev-safety)
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, Math.min(bytes.length, 32));
            for (int i = bytes.length; i < 32; i++) padded[i] = (byte) i;
            bytes = padded;
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String generateToken(UserDetails user) {
        Instant now = Instant.now();
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return parseAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails user) {
        Claims claims = parseAllClaims(token);
        String username = claims.getSubject();
        Date exp = claims.getExpiration();
        return username != null && username.equals(user.getUsername()) && exp != null && exp.after(new Date());
    }

    private Claims parseAllClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
