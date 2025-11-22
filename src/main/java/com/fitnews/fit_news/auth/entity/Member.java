package com.fitnews.fit_news.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String password; // ✅ LOCAL은 값 있음, SOCIAL은 null 가능

    private String name;
    private String email;

    // ✅ Refresh Token 필드
    private String refreshToken;

    private boolean onboardingCompleted = false;

    // ✅ 여기부터 소셜/로그인 타입 추가
    @Enumerated(EnumType.STRING)
    private LoginType loginType;   // LOCAL, GOOGLE, KAKAO

    private String provider;       // "google", "kakao"
    private String providerId;     // 소셜에서 넘어온 고유 ID
}
