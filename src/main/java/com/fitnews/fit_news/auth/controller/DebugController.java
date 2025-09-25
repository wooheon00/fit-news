package com.fitnews.fit_news.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

@RestController
public class DebugController {

    @GetMapping("/api/debug-test")
    public String test(Authentication auth) {
        return "현재 로그인 유저: " + (auth != null ? auth.getName() : "없음");
    }
}
