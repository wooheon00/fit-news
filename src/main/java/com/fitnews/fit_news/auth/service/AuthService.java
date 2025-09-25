package com.fitnews.fit_news.auth.service;

import com.fitnews.fit_news.auth.dto.TokenResponse;
import com.fitnews.fit_news.auth.entity.Member;
import com.fitnews.fit_news.auth.jwt.JwtTokenProvider;
import com.fitnews.fit_news.auth.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public String register(String username, String password, String email) {
        if (memberRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }
        String encodedPw = passwordEncoder.encode(password);
        Member member = new Member(null, username, encodedPw, null, email, null);
        memberRepository.save(member);
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

        // ✅ Refresh 토큰 저장
        member.setRefreshToken(refreshToken);
        memberRepository.save(member);

        return new TokenResponse(accessToken, refreshToken);
    }
}