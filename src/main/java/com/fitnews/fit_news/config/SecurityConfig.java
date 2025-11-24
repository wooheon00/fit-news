package com.fitnews.fit_news.config;

import com.fitnews.fit_news.auth.jwt.JwtAuthenticationFilter;
import com.fitnews.fit_news.auth.social.CustomOAuth2UserService;
import com.fitnews.fit_news.auth.social.OAuth2LoginSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                .authorizeHttpRequests(auth -> auth
                        // ✅ 정적 리소스는 항상 허용
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**"
                        ).permitAll()

                        // ✅ 로그인/회원가입, OAuth2, 소셜 콜백, 공개 페이지들 허용
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/api/auth/**",
                                "/h2-console/**",
                                "/oauth2/**",
                                "/social-login/**",
                                "/news",
                                "/news-tendencies",
                                "/users",
                                "/logs",
                                "/member-preferences",
                                "/onboarding",          // 온보딩 페이지
                                "/recommend",
                                "/main"// ⭐ 페이지는 열어둔다
                        ).permitAll()

                        // ✅ 추천 뉴스 "API"만 인증 필요
                        .requestMatchers("/api/recommend/**").authenticated()

                        // 나머지는 일단 오픈 (원하면 나중에 조이기)
                        .anyRequest().permitAll()
                )

                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2LoginSuccessHandler)
                );

        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
