package com.fitnews.fit_news.auth.social;

import com.fitnews.fit_news.auth.entity.Member;
import com.fitnews.fit_news.auth.service.SocialMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService
        implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final SocialMemberService socialMemberService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate =
                new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId =
                userRequest.getClientRegistration().getRegistrationId(); // google, kakao

        // ✅ 여기서 provider별 userNameAttributeName 가져오기 (google=sub, kakao=id)
        String userNameAttributeName =
                userRequest.getClientRegistration()
                        .getProviderDetails()
                        .getUserInfoEndpoint()
                        .getUserNameAttributeName();

        Map<String, Object> attributes = oAuth2User.getAttributes();

        log.info("🌐 provider = {}", registrationId);
        log.info("🌐 userNameAttributeName = {}", userNameAttributeName);
        log.info("🌐 raw attributes = {}", attributes);

        OAuthAttributes extracted = OAuthAttributes.of(registrationId, attributes);

        socialMemberService.saveOrUpdate(
                extracted.getProvider(),
                extracted.getProviderId(),
                extracted.getName(),
                extracted.getEmail()
        );

        // ✅ 여기서도 userNameAttributeName 사용
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                userNameAttributeName   // google이면 sub, kakao면 id
        );
    }
}

