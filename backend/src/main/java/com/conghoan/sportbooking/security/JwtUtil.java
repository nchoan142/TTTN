package com.conghoan.sportbooking.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Tạo ra token chứa Email và userId
    public String generateToken(String email, Long userId) {
        return Jwts.builder()
                .subject(email) // tạo token cho email nào
                .claim("userId", userId) // truyền thêm userId qua token
                .issuedAt(new Date()) // thời điểm tạo token
                .expiration(new Date(System.currentTimeMillis() + expiration)) // thời gian hết hạn của token
                .signWith(getSigningKey())
                .compact();
    }

    // lấy email từ token
    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // xác minh key
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
