package com.fitnews.fit_news.auth.social;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthAttributes {

    private final String provider;     // google, kakao
    private final String providerId;   // sub / id
    private final String name;
    private final String email;
    private final Map<String, Object> attributes;

    @Builder
    public OAuthAttributes(String provider, String providerId,
                           String name, String email,
                           Map<String, Object> attributes) {
        this.provider = provider;
        this.providerId = providerId;
        this.name = name;
        this.email = email;
        this.attributes = attributes;
    }

    public static OAuthAttributes of(String registrationId,
                                     Map<String, Object> attributes) {
        if ("kakao".equals(registrationId)) {
            return ofKakao(attributes);
        }
        return ofGoogle(attributes);
    }

    private static OAuthAttributes ofGoogle(Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .provider("google")
                .providerId(attributes.get("sub").toString())
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .attributes(attributes)
                .build();
    }

    private static OAuthAttributes ofKakao(Map<String, Object> attributes) {
        Long id = (Long) attributes.get("id");

        Map<String, Object> kakaoAccount =
                (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile =
                (Map<String, Object>) kakaoAccount.get("profile");

        return OAuthAttributes.builder()
                .provider("kakao")
                .providerId(id.toString())
                .name((String) profile.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                .attributes(attributes)
                .build();
    }
}

