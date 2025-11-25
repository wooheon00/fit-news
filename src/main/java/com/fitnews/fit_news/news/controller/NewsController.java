package com.fitnews.fit_news.news.controller;

import com.fitnews.fit_news.news.entity.News;
import com.fitnews.fit_news.news.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;
    private final NewsClassificationService newsClassificationService;
    private final NewsCrawlingService newsCrawlingService;
    private final NewsRecommendationService newsRecommendationService;
    private final NewsScheduler newsScheduler;
    private final OpenAIAPIService openAIAPIService;

    @GetMapping("/")
    public String newsList(Model model) {
        List<News> newsList = newsService.getAllNews();

        Map<String, List<News>> newsBySource = newsList.stream()
                .collect(Collectors.groupingBy(news -> newsClassificationService.detectSourceFromLink(news.getLink())));

        model.addAttribute("newsList", newsBySource);
        return "main"; // templates/news.html
    }

}
