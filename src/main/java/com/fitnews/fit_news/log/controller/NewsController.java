package com.fitnews.fit_news.log.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NewsController {


    @GetMapping("/dummy")
    public String dummyPage() {
        return "dummy";  // templates/dummy.html
    }
}
