package com.fitnews.fit_news.memberPreference.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OnboardingPageController {

    @GetMapping("/onboarding")
    public String onboardingPage() {
        return "onboarding";   // templates/onboarding.html
    }
}
