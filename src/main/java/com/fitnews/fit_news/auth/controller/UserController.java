package com.fitnews.fit_news.auth.controller;

import com.fitnews.fit_news.auth.dto.TokenResponse;
import com.fitnews.fit_news.auth.entity.Member;
import com.fitnews.fit_news.auth.repository.MemberRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

@RestController
public class UserController {

    // 현재 로그인한 사용자 정보 반환
    @GetMapping("/api/me")
    public String me(Authentication authentication) {
        if (authentication == null) {
            return "❌ 로그인 안됨";
        }
        return authentication.getName() + "님 로그인";
    }
}