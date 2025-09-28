package com.fitnews.fit_news.news.controller;

import com.fitnews.fit_news.news.entity.News;
import com.fitnews.fit_news.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping("/news")
    public String newsList(Model model) {
        List<News> newsList = newsService.getAllNews();
        model.addAttribute("newsList", newsList);
        return "news"; // templates/news.html
    }
}
