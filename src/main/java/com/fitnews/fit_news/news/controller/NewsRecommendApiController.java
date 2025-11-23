package com.fitnews.fit_news.news.controller;

import com.fitnews.fit_news.auth.service.AuthService;
import com.fitnews.fit_news.news.dto.NewsDto;
import com.fitnews.fit_news.news.dto.RecommendedResponse;
import com.fitnews.fit_news.news.dto.RecommendedResult;
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
    public RecommendedResponse recommend(HttpServletRequest request) {

        Long memberId = authService.getMemberIdFromRequest(request);

        RecommendedResult result = newsRecommendationService
                .recommendWithOpposite(memberId, 3, 2);  // 상위 3, 반대 2

        return RecommendedResponse.from(result);
    }
}

