package com.fitnews.fit_news.log.service;

import com.fitnews.fit_news.log.dto.ClickLogRequest;
import com.fitnews.fit_news.log.entity.ClickLog;
import com.fitnews.fit_news.log.repository.ClickLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClickLogService {

    private final ClickLogRepository clickLogRepository;

    public void saveLog(Long userId, ClickLogRequest request) {
        ClickLog log = new ClickLog();
        log.setUserId(userId);
        log.setNewsId(request.getNewsId());
        clickLogRepository.save(log);
    }
}
