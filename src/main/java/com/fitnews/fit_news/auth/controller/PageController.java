package com.fitnews.fit_news.auth.controller;

import com.fitnews.fit_news.auth.entity.Member;
import com.fitnews.fit_news.auth.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final MemberRepository memberRepository;

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/news")
    public String news() {
        return "news"; // ✅ 서버에서는 그냥 페이지 렌더링만
    }

    @GetMapping("/users")
    public String users(Model model) {
        List<Member> members = memberRepository.findAll();
        model.addAttribute("users", members);
        return "users";
    }
}
