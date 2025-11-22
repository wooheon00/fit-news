package com.fitnews.fit_news.auth.controller;

import com.fitnews.fit_news.auth.dto.TokenResponse;
import com.fitnews.fit_news.auth.entity.Member;
import com.fitnews.fit_news.auth.repository.MemberRepository;
import com.fitnews.fit_news.auth.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final MemberRepository memberRepository;

    @GetMapping("/api/me")
    public ResponseEntity<String> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("(로그인 안됨)");
        }

        String username = authentication.getName();  // JWT subject = username

        Member member = memberRepository.findByUsername(username)
                .orElse(null);

        String displayName = (member != null && member.getName() != null && !member.getName().isBlank())
                ? member.getName()
                : username; // name 없으면 username fallback

        return ResponseEntity.ok("로그인중 : " + displayName);
    }
}