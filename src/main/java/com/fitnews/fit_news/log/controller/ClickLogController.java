package com.fitnews.fit_news.log.controller;

import com.fitnews.fit_news.auth.security.CustomUserDetails;
import com.fitnews.fit_news.log.dto.ClickLogRequest;
import com.fitnews.fit_news.log.service.ClickLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class ClickLogController {

    private final ClickLogService clickLogService;

    @PostMapping("/click")
    public ResponseEntity<Void> logClick(@RequestBody ClickLogRequest request,
                                         @AuthenticationPrincipal CustomUserDetails user) {
        clickLogService.saveLog(user.getId(), request);
        return ResponseEntity.ok().build();
    }
}
