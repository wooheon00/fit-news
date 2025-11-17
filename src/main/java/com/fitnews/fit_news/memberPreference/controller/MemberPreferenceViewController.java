package com.fitnews.fit_news.memberPreference.controller;

import com.fitnews.fit_news.memberPreference.entity.MemberPreference;
import com.fitnews.fit_news.memberPreference.repository.MemberPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberPreferenceViewController {

    private final MemberPreferenceRepository memberPreferenceRepository;

    @GetMapping("/member-preferences")
    public String list(Model model) {
        List<MemberPreference> prefs = memberPreferenceRepository.findAll();
        model.addAttribute("prefs", prefs);
        return "member-preferences";
    }
}

