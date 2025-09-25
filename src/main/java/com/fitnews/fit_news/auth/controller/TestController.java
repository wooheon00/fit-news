package com.fitnews.fit_news.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public String test(Authentication authentication) {
        if (authentication == null) {
            return "로그인 필요!";
        }
        return "인증 성공! 유저: " + authentication.getName();
    }
}
