package com.fitnews.fit_news.log.controller;

import com.fitnews.fit_news.auth.security.CustomUserDetails;
import com.fitnews.fit_news.log.dto.ClickLogRequest;
import com.fitnews.fit_news.log.entity.ClickLog;
import com.fitnews.fit_news.log.repository.ClickLogRepository;
import com.fitnews.fit_news.log.service.ClickLogService;
import com.fitnews.fit_news.news.entity.News;
import com.fitnews.fit_news.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class ClickLogController {

    private final NewsRepository newsRepository;
    private final ClickLogService clickLogService;

    @PostMapping("/click")
    public ResponseEntity<String> logClick(@RequestBody ClickLogRequest req,
                                           @AuthenticationPrincipal CustomUserDetails user) {

        News news = newsRepository.findById(req.getNewsId())
                .orElseThrow(() -> new IllegalArgumentException("뉴스를 찾을 수 없습니다: " + req.getNewsId()));

        // ✅ 로그인 된 경우에만 로그 + 성향 업데이트
        if (user != null) {
            clickLogService.logClick(user.getId(), news.getId());
        }

        // 링크 반환 → 프론트에서 이 링크로 이동
        return ResponseEntity.ok(news.getLink());
    }
}
