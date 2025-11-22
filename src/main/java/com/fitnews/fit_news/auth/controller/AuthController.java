package com.fitnews.fit_news.auth.controller;

import com.fitnews.fit_news.auth.dto.LoginRequest;
import com.fitnews.fit_news.auth.dto.RegisterRequest;
import com.fitnews.fit_news.auth.dto.TokenResponse;
import com.fitnews.fit_news.auth.entity.Member;
import com.fitnews.fit_news.auth.jwt.JwtTokenProvider;
import com.fitnews.fit_news.auth.repository.MemberRepository;
import com.fitnews.fit_news.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
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



    // 🔥 수정된 부분
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        // 1) JWT 기반 로그아웃 (RefreshToken 삭제)
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtTokenProvider.validateToken(token)) {
                String username = jwtTokenProvider.getUsername(token);

                Member member = memberRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자: " + username));

                // ✅ DB에서 Refresh Token 제거
                member.setRefreshToken(null);
                memberRepository.save(member);
            }
        }

        // 2) 세션 기반(OAuth2) 로그아웃까지 같이 처리
        //    - HttpSession 무효화
        //    - SecurityContext 정리
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        } else {
            SecurityContextHolder.clearContext();
        }

        return ResponseEntity.ok("로그아웃 성공");
    }


    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(401).build();
        }

        String username = jwtTokenProvider.getUsername(refreshToken);
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자: " + username));

        if (member.getRefreshToken() == null || !refreshToken.equals(member.getRefreshToken())) {
            return ResponseEntity.status(401).build();
        }

        // 새로운 AccessToken 발급
        String newAccessToken = jwtTokenProvider.generateAccessToken(username);

        // 🔥 Member의 온보딩 완료 여부 넣기
        boolean needOnboarding = !member.isOnboardingCompleted();

        return ResponseEntity.ok(
                new TokenResponse(newAccessToken, refreshToken, needOnboarding)
        );
    }

}

