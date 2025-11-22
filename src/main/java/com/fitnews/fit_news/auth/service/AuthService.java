package com.fitnews.fit_news.auth.service;

import com.fitnews.fit_news.auth.dto.TokenResponse;
import com.fitnews.fit_news.auth.entity.LoginType;
import com.fitnews.fit_news.auth.entity.Member;
import com.fitnews.fit_news.auth.jwt.JwtTokenProvider;
import com.fitnews.fit_news.auth.repository.MemberRepository;
import com.fitnews.fit_news.memberPreference.service.MemberPreferenceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberPreferenceService memberPreferenceService;

    public String register(String username, String password, String email) {
        if (memberRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        Member member = new Member();
        member.setUsername(username);
        member.setPassword(passwordEncoder.encode(password));
        member.setEmail(email);
        member.setLoginType(LoginType.LOCAL);
        member.setProvider(null);
        member.setProviderId(null);

        memberRepository.save(member);
        memberPreferenceService.createDefaultFor(member);

        return "회원가입 성공";

    }

    public TokenResponse login(String username, String password) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자"));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("비밀번호 불일치");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(member.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(member.getUsername());

        member.setRefreshToken(refreshToken);
        memberRepository.save(member);

        // ✅ 온보딩이 필요한지 판단:
        // 1) onboardingCompleted=false 이고
        // 2) MemberPreference가 존재(혹은 기본 생성)하는지
        boolean needOnboarding = !member.isOnboardingCompleted();

        // 혹시 MemberPreference가 없는 상태라면 기본값 생성 (안전장치)
        if (!memberPreferenceService.existsFor(member)) {
            memberPreferenceService.createDefaultFor(member);
        }

        return new TokenResponse(accessToken, refreshToken, needOnboarding);
    }

    public Long getMemberIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "토큰 없음");
        }

        String token = authHeader.substring(7); // "Bearer " 잘라내기

        // JwtTokenProvider 안에 이런 메서드가 있다고 가정 (없으면 추가해야 함)
        String username = jwtTokenProvider.getUsernameFromToken(token);

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "존재하지 않는 사용자"
                ));

        return member.getId();
    }

}