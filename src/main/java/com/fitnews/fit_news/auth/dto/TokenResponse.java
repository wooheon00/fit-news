package com.fitnews.fit_news.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private boolean needOnboarding;   // ✅ 온보딩 필요 여부
}

