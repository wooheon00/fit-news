package com.fitnews.fit_news.memberPreference.controller;

import com.fitnews.fit_news.auth.entity.Member;
import com.fitnews.fit_news.auth.repository.MemberRepository;
import com.fitnews.fit_news.memberPreference.entity.MemberPreference;
import com.fitnews.fit_news.memberPreference.service.MemberPreferenceService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final MemberRepository memberRepository;
    private final MemberPreferenceService memberPreferenceService;

    @Getter
    public static class OnboardingRequest {
        private String name;
        private String age;
        private int gender;
        private int politic;
    }

    @PostMapping("/me")
    public ResponseEntity<Void> saveMyOnboarding(@RequestBody OnboardingRequest req,
                                                 Authentication auth) {
        String username = auth.getName();

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("회원 정보 없음: " + username));

        // Member 기본 정보 업데이트
        member.setName(req.getName());
        member.setOnboardingCompleted(true);
        memberRepository.save(member);

        // age는 char 한 글자로 사용 ('a'/'b'/'c'/'d')
        char ageChar = (req.getAge() != null && !req.getAge().isEmpty())
                ? req.getAge().charAt(0)
                : 'b';

        // ✅ 온보딩용 preference 업데이트
        memberPreferenceService.updateForOnboarding(
                member,
                req.getPolitic(),
                req.getGender(),
                ageChar
        );

        return ResponseEntity.ok().build();
    }
}
