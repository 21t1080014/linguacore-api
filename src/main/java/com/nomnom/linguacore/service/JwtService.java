package com.nomnom.linguacore.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration-ms}")
    private long expirationMs;
    private SecretKey key(){
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email,Long userId,String role){
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder().subject(email)
                .claim("userId", userId)
                .claim("role", role)    //dữ liệu thêm
                .issuedAt(now)               // thời điểm phát
                .expiration(expiry)          // thời điểm hết hạn
                .signWith(key())             // ký bằng khóa bí mật
                .compact();
    }

    public String extractEmail(String token){
        return parseClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key())     // kiểm chữ ký
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
