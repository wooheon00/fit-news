package com.fitnews.fit_news.news.service;

import com.fitnews.fit_news.log.repository.ClickLogRepository;
import com.fitnews.fit_news.news.repository.NewsRepository;
import com.fitnews.fit_news.news.repository.NewsTendencyRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NewsCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(NewsCleanupScheduler.class);

    private final NewsRepository newsRepository;
    private final NewsTendencyRepository newsTendencyRepository;
    private final ClickLogRepository clickLogRepository;

    /**
     * 매일 새벽 4시에
     *  - 1주일 지난 뉴스 + 연관된 NewsTendency + ClickLog 삭제
     */
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void cleanupOldNews() {
        LocalDateTime cutoff = LocalDateTime.now().minusWeeks(1);

        // 1) 기준보다 오래된 뉴스 ID 조회
        List<Long> oldNewsIds = newsRepository.findIdsByPubDateBefore(cutoff);
        if (oldNewsIds.isEmpty()) {
            log.info("🧹 삭제할 오래된 뉴스가 없습니다. (cutoff={})", cutoff);
            return;
        }

        log.info("🧹 {}개 뉴스가 1주일 이상 경과, 정리 시작. ids={}", oldNewsIds.size(), oldNewsIds);

        // 2) 클릭 로그 삭제
        int deletedLogs = clickLogRepository.deleteByNewsIds(oldNewsIds);
        log.info("🧹 ClickLog 삭제: {}건", deletedLogs);

        // 3) 뉴스 성향 삭제
        int deletedTendencies = newsTendencyRepository.deleteByNewsIds(oldNewsIds);
        log.info("🧹 NewsTendency 삭제: {}건", deletedTendencies);

        // 4) 마지막으로 뉴스 삭제
        int deletedNews = newsRepository.deleteByIdIn(oldNewsIds);
        log.info("🧹 News 삭제: {}건", deletedNews);

        log.info("✅ 오래된 뉴스 정리 완료 (cutoff={})", cutoff);
    }
}
