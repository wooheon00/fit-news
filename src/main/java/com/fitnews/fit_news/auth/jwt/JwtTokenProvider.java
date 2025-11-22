package com.fitnews.fit_news.auth.jwt;

import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;


@Component
public class JwtTokenProvider {

    private String secretKey = "mySuperSecretKeyForJwtTokenThatIsAtLeast32Bytes!";


    private final long accessTokenValidTime = 1000L * 60 * 60;
    private final long refreshTokenValidTime = 1000L * 60 * 60 * 24;

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
            // 🔥 1) null / 빈문자열 방어
            if (token == null || token.isBlank()) {
                return false;
            }

            // 🔥 2) JWT 형식(aaa.bbb.ccc)인지 먼저 확인
            long dotCount = token.chars().filter(ch -> ch == '.').count();
            if (dotCount != 2) {
                // 형식 자체가 이상한 건 그냥 false만 주고 로그는 안 찍음
                return false;
            }

            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token);

            Date expiration = claims.getBody().getExpiration();
            boolean valid = expiration.after(new Date());

            if (!valid) {
                System.out.println("[JwtTokenProvider] 토큰 만료됨");
            }
            return valid;
        } catch (ExpiredJwtException e) {
            System.out.println("[JwtTokenProvider] 토큰 만료됨(예외): " + e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            // 실제 디버깅 필요할 때만 보고, 평소에는 시끄럽지 않게
            System.out.println("[JwtTokenProvider] 토큰 검증 실패(JwtException): " + e.getClass().getSimpleName());
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

