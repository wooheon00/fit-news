package com.fitnews.fit_news.auth.controller;

import com.fitnews.fit_news.auth.dto.LoginRequest;
import com.fitnews.fit_news.auth.dto.RegisterRequest;
import com.fitnews.fit_news.auth.dto.TokenResponse;
import com.fitnews.fit_news.auth.entity.Member;
import com.fitnews.fit_news.auth.jwt.JwtTokenProvider;
import com.fitnews.fit_news.auth.repository.MemberRepository;
import com.fitnews.fit_news.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request.getUsername(), request.getPassword(), request.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request.getUsername(), request.getPassword()));
    }



    @PostMapping("/logout")
    public ResponseEntity<String> logout(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).body("로그인 필요");

        String username = auth.getName();
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자"));

        // ✅ Refresh Token 제거
        member.setRefreshToken(null);
        memberRepository.save(member);

        return ResponseEntity.ok("로그아웃 성공");
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        System.out.println("[/api/auth/refresh] 요청: " + refreshToken);

        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            System.out.println("[/api/auth/refresh] refresh 토큰 유효하지 않음");
            return ResponseEntity.status(401).build();
        }

        String username = jwtTokenProvider.getUsername(refreshToken);
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자: " + username));

        if (member.getRefreshToken() == null || !refreshToken.equals(member.getRefreshToken())) {
            System.out.println("[/api/auth/refresh] DB refreshToken 불일치");
            return ResponseEntity.status(401).build();
        }

        // ✅ AccessToken만 새로 발급
        String newAccessToken = jwtTokenProvider.generateAccessToken(username);

        System.out.println("[/api/auth/refresh] AccessToken 재발급 완료 → user=" + username);

        // 기존 RefreshToken 그대로 반환
        return ResponseEntity.ok(new TokenResponse(newAccessToken, refreshToken));
    }

}

