package com.fitnews.fit_news.news.controller;

import com.fitnews.fit_news.auth.service.AuthService;
import com.fitnews.fit_news.news.dto.NewsDto;
import com.fitnews.fit_news.news.entity.News;
import com.fitnews.fit_news.news.service.NewsRecommendationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NewsRecommendApiController {

    private final AuthService authService;
    private final NewsRecommendationService newsRecommendationService;

    @GetMapping("/recommend")
    public List<NewsDto> recommend(HttpServletRequest request) {

        Long memberId = authService.getMemberIdFromRequest(request);

        // 🔥 로그인 유저 기준 추천 뉴스 3개
        List<News> newsList = newsRecommendationService.recommend(memberId, 3);

        return newsList.stream()
                .map(NewsDto::from)
                .toList();
    }
}

