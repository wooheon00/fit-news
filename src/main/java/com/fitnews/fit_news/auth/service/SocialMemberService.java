package com.fitnews.fit_news.auth.service;

import com.fitnews.fit_news.auth.entity.LoginType;
import com.fitnews.fit_news.auth.entity.Member;
import com.fitnews.fit_news.auth.repository.MemberRepository;
import com.fitnews.fit_news.memberPreference.repository.MemberPreferenceRepository;
import com.fitnews.fit_news.memberPreference.service.MemberPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SocialMemberService {

    private final MemberRepository memberRepository;
    private final MemberPreferenceService memberPreferenceService;

    /**
     * 소셜 로그인으로부터 받은 정보로 Member를 생성/업데이트하고 반환
     */
    public Member saveOrUpdate(String provider, String providerId,
                               String name, String email) {

        // username은 내부에서 쓰는 고유 키 (JWT subject도 이걸로 사용)
        String username = provider + "_" + providerId;  // ex) google_123456789

        return memberRepository.findByUsername(username)
                .map(member -> {
                    member.setName(name);
                    member.setEmail(email);
                    Member saved = memberRepository.save(member);

                    // ✅ 혹시 예전에 Member만 만들고 Preference는 없을 수도 있으니까
                    if (!memberPreferenceService.existsFor(saved)) {
                        memberPreferenceService.createDefaultFor(saved);
                    }

                    return saved;
                })
                .orElseGet(() -> {
                    Member member = new Member();
                    member.setUsername(username);
                    member.setPassword(null);
                    member.setName(name);
                    member.setEmail(email);
                    member.setRefreshToken(null);
                    member.setLoginType(
                            "google".equals(provider) ? LoginType.GOOGLE : LoginType.KAKAO
                    );
                    member.setProvider(provider);
                    member.setProviderId(providerId);

                    Member saved = memberRepository.save(member);
                    memberPreferenceService.createDefaultFor(saved);
                    return saved;
                });
    }
}
