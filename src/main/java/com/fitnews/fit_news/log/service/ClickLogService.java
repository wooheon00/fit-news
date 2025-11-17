package com.fitnews.fit_news.log.service;

import com.fitnews.fit_news.log.dto.ClickLogRequest;
import com.fitnews.fit_news.log.entity.ClickLog;
import com.fitnews.fit_news.log.repository.ClickLogRepository;
import com.fitnews.fit_news.memberPreference.service.MemberPreferenceService;
import com.fitnews.fit_news.news.entity.NewsTendency;
import com.fitnews.fit_news.news.repository.NewsTendencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClickLogService {

    private final ClickLogRepository clickLogRepository;
    private final NewsTendencyRepository newsTendencyRepository;
    private final MemberPreferenceService memberPreferenceService;

    @Transactional
    public void logClick(Long memberId, Long newsId) {

        // 1) 클릭 로그 저장
        ClickLog log = new ClickLog();
        log.setUserId(memberId);
        log.setNewsId(newsId);
        clickLogRepository.save(log);

        // 2) 해당 뉴스 성향 가져오기
        NewsTendency tendency = newsTendencyRepository.findByNewsId(newsId)
                .orElseThrow(() -> new IllegalArgumentException("NewsTendency 없음: " + newsId));

        // 3) 회원 성향 업데이트
        memberPreferenceService.updatePreference(memberId, tendency);
    }
}
