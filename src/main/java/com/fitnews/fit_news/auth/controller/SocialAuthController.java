package com.fitnews.fit_news.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SocialAuthController {

    @GetMapping("/social-login/success")
    public String socialLoginSuccess(@RequestParam String accessToken,
                                     @RequestParam String refreshToken,
                                     @RequestParam(defaultValue = "false") boolean needOnboarding,
                                     Model model) {

        model.addAttribute("accessToken", accessToken);
        model.addAttribute("refreshToken", refreshToken);
        model.addAttribute("needOnboarding", needOnboarding);

        return "social-login-success"; // templates/social-login-success.html
    }
}
