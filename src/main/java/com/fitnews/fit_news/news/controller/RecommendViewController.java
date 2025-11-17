package com.fitnews.fit_news.news.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RecommendViewController {

    @GetMapping("/recommend")
    public String recommendPage() {
        // 로그인 체크는 JS + /api/recommend 쪽에서 처리
        return "recommend";
    }
}
