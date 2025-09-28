package com.fitnews.fit_news.log.controller;

import com.fitnews.fit_news.log.entity.ClickLog;
import com.fitnews.fit_news.log.repository.ClickLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LogPageController {

    private final ClickLogRepository clickLogRepository;

    @GetMapping("/logs")
    public String logs(Model model) {
        model.addAttribute("logs", clickLogRepository.findAll());
        return "logs";   // templates/logs.html
    }
}