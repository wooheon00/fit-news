package com.fitnews.fit_news.config;

import com.fitnews.fit_news.auth.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                        .accessDeniedHandler((req, res, e) -> res.sendError(HttpServletResponse.SC_FORBIDDEN))
                )
                .authorizeHttpRequests(auth -> auth
                        // 정적 리소스
                        .requestMatchers("/js/**", "/css/**", "/images/**", "/favicon.ico").permitAll()

                        // 페이지(뷰) — 로그인 없이 접근 허용
                        .requestMatchers("/", "/register", "/login", "/news", "/users", "/dummy", "/logs", "/error").permitAll()

                        // 인증/토큰 관련 API 허용
                        .requestMatchers("/api/auth/**").permitAll()

                        // 그 외 API는 인증 필요
                        .requestMatchers("/api/**").authenticated()

                        // 그 외 나머지(예: 추가 페이지)도 우선 열어두고 테스트 편하게
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
