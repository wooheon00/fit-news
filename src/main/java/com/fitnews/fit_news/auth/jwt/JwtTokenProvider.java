package com.fitnews.fit_news.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;


@Component
public class JwtTokenProvider {

    private String secretKey = "mySuperSecretKeyForJwtTokenThatIsAtLeast32Bytes!";

    // Access 10초, Refresh 25초
    private final long accessTokenValidTime = 1000L * 10;
    private final long refreshTokenValidTime = 1000L * 25;

    @PostConstruct
    public void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String generateAccessToken(String username) {
        String token = createToken(username, accessTokenValidTime);
        System.out.println("[JwtTokenProvider] AccessToken 발급: " + username + " (유효: " + accessTokenValidTime/1000 + "초)");
        return token;
    }

    public String generateRefreshToken(String username) {
        String token = createToken(username, refreshTokenValidTime);
        System.out.println("[JwtTokenProvider] RefreshToken 발급: " + username + " (유효: " + refreshTokenValidTime/1000 + "초)");
        return token;
    }

    private String createToken(String username, long validTime) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + validTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token);

            Date expiration = claims.getBody().getExpiration();
            boolean valid = expiration.after(new Date());

            if (!valid) {
                System.out.println("[JwtTokenProvider] 토큰 만료됨");
            }
            return valid;
        } catch (Exception e) {
            System.out.println("[JwtTokenProvider] 토큰 검증 실패: " + e.getMessage());
            return false;
        }
    }

    public String getUsername(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}

