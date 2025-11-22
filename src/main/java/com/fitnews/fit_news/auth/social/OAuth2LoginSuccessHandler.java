package com.fitnews.fit_news.auth.social;

import com.fitnews.fit_news.auth.entity.Member;
import com.fitnews.fit_news.auth.jwt.JwtTokenProvider;
import com.fitnews.fit_news.auth.repository.MemberRepository;
import com.fitnews.fit_news.memberPreference.service.MemberPreferenceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final MemberPreferenceService memberPreferenceService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = oauthToken.getAuthorizedClientRegistrationId(); // google, kakao

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String username = extractUsername(registrationId, oAuth2User); // google_123....

        log.info("[OAuth2LoginSuccessHandler] provider={}, username={}", registrationId, username);

        String accessToken = jwtTokenProvider.generateAccessToken(username);
        String refreshToken = jwtTokenProvider.generateRefreshToken(username);

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("소셜 회원 정보가 DB에 없음: " + username));

        // ✅ refreshToken 저장
        member.setRefreshToken(refreshToken);
        memberRepository.save(member);

        // ✅ MemberPreference 기본값 보장 (없으면 생성)
        if (!memberPreferenceService.existsFor(member)) {
            memberPreferenceService.createDefaultFor(member);
        }

        // ✅ 온보딩 필요 여부 (로컬 로그인과 동일한 기준 사용)
        boolean needOnboarding = !member.isOnboardingCompleted();

        // ✅ needOnboarding 도 쿼리스트링에 같이 실어서 보냄
        String redirectUrl = "/social-login/success"
                + "?accessToken=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
                + "&refreshToken=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8)
                + "&needOnboarding=" + needOnboarding;

        log.info("[OAuth2LoginSuccessHandler] redirecting to {}", redirectUrl);

        response.sendRedirect(redirectUrl);
    }

    /**
     * provider + providerId 로 우리 서비스 username 생성
     * (SocialMemberService에서 Member 생성할 때랑 반드시 규칙이 같아야 함!)
     */
    private String extractUsername(String registrationId, OAuth2User oAuth2User) {
        if ("google".equals(registrationId)) {
            String sub = oAuth2User.getAttribute("sub"); // 구글 고유 ID
            return "google_" + sub;
        } else if ("kakao".equals(registrationId)) {
            Long id = oAuth2User.getAttribute("id"); // 카카오 고유 ID
            return "kakao_" + id;
        } else {
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인: " + registrationId);
        }
    }
}
