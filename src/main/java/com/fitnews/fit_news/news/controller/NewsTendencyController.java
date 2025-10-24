package com.fitnews.fit_news.news.controller;

import com.fitnews.fit_news.news.entity.NewsTendency;
import com.fitnews.fit_news.news.repository.NewsTendencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class NewsTendencyController {

    private final NewsTendencyRepository newsTendencyRepository;

    @GetMapping("/news-tendencies")
    public String showTendencies(Model model) {
        List<NewsTendency> tendencies = newsTendencyRepository.findAll();
        model.addAttribute("tendencies", tendencies);
        return "newsTendency"; // templates/newsTendency.html
    }
}