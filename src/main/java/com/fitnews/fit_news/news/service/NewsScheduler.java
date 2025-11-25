package com.fitnews.fit_news.news.service;

import com.fitnews.fit_news.news.entity.News;
import com.fitnews.fit_news.news.model.NewsData;
import com.fitnews.fit_news.news.repository.NewsRepository;
import com.fitnews.fit_news.news.repository.NewsTendencyRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class NewsScheduler {
    private static final Logger logger = LoggerFactory.getLogger(NewsScheduler.class);

    // 한 번에 5개씩 분류
    private static final int BATCH_SIZE = 5;

    private final NewsCrawlingService newsCrawlingService;
    private final OpenAIAPIService openAIAPIService;
    private final NewsClassificationService newsClassificationService;
    private final NewsTendencyService newsTendencyService;
    private final NewsService newsService;

    // 중복(이미 저장된 뉴스/성향) 체크용
    private final NewsRepository newsRepository;
    private final NewsTendencyRepository newsTendencyRepository;

    /**
     *  ✅ 서버 시작 후 1번 즉시 실행
     */
    @EventListener(ApplicationReadyEvent.class)
    public void runOnStartup() {
        logger.info("Application started. Running initial crawling job.");
        runCrawlingJob();
    }


    @Scheduled(fixedRate = 30 * 60 * 1000L)
    public void runCrawlingJob() {
        long crawlingTime = System.currentTimeMillis();
        logger.info("NewsScheduler triggered at {}", crawlingTime);

        try {
            // 1) 크롤링
            List<NewsData> rawNews = newsCrawlingService.crawlingNews(crawlingTime);
            if (rawNews == null || rawNews.isEmpty()) {
                logger.info("No new news items found at {}", crawlingTime);
                return;
            }
            logger.info("✅ 1 Crawling Clear: {} items", rawNews.size());

            // 2) 링크 정규화 + 배치내 중복 제거
            List<NewsData> deDuped = deDuplicateInBatch(rawNews);
            logger.info("✅ De-dup in batch: {} -> {}", rawNews.size(), deDuped.size());

            // 3) 언론사별로 그룹핑 + DB 기준 중복 제거 + 언론사별 최신 10개만
            Map<String, List<NewsData>> groupedBySource = deDuped.stream()
                    .collect(Collectors.groupingBy(nd ->
                            newsClassificationService.detectSourceFromLink(nd.getLink())
                    ));

            List<NewsData> onlyNew = groupedBySource.values().stream()
                    .flatMap(listForSource ->
                            listForSource.stream()
                                    // DB에 이미 있는 링크는 제외
                                    .filter(nd -> !newsRepository.existsByLink(nd.getLink()))
                                    // pubDate 기준 최신순 정렬
                                    .sorted(Comparator.comparing(
                                            NewsData::getPubDate,
                                            Comparator.nullsLast(Comparator.naturalOrder())
                                    ).reversed())
                                    // 🔥 언론사별 상위 10개만
                                    .limit(10)
                    )
                    .toList();

            if (onlyNew.isEmpty()) {
                logger.info("All crawled items already exist in DB (by source). Nothing to analyze/save.");
                return;
            }
            logger.info("✅ After DB de-dup & per-source limit: {} items to analyze", onlyNew.size());

            // 4) 배치 분류/저장
            int totalSavedNews = 0;
            int totalSavedTendency = 0;
            int totalSkippedNews = 0;

            for (int i = 0; i < onlyNew.size(); i += BATCH_SIZE) {
                List<NewsData> batch = onlyNew.subList(i, Math.min(i + BATCH_SIZE, onlyNew.size()));
                int batchIdx = (i / BATCH_SIZE) + 1;

                logger.info("➡️ Batch {} start (size={})", batchIdx, batch.size());

                String prompt = newsClassificationService.preprocessingNews(batch);
                logger.info("✅ 2 Preprocessing Success (batch={})", batchIdx);

                String response = openAIAPIService.askChatGPT(prompt);

                // 에러/비정상 응답이면 분류 스킵하고 뉴스만 저장
                if (checkingResponseValidForBatch(response, batch, batchIdx)) {
                    // 이미 저장/로깅 끝
                    continue;
                }
                logger.info("✅ 3 Request Success (batch={})", batchIdx);

                // 후처리(분류 결과 주입)
                List<NewsData> classified = newsClassificationService.postprocessingNews(response, batch);
                logger.info("✅ 4 PostProcessing Success (batch={})", batchIdx);

                // 저장: News → (있다면) NewsTendency
                int savedNewsCnt = 0, savedTendencyCnt = 0, skippedNewsCnt = 0;
                for (NewsData nd : classified) {

                    // 🔥 1) 성향(Tc)이 없으면 이 뉴스는 통째로 스킵
                    if (nd.getNewsTc() == null) {
                        skippedNewsCnt++;
                        logger.warn("⚠️ No NewsTendency for this news. Skip saving. title={}, link={}",
                                nd.getTitle(), nd.getLink());
                        continue;
                    }

                    // 2) DTO → News 저장 (중복은 내부에서 방지)
                    News savedNews = newsService.saveNews(nd.toNewsEntity());
                    if (savedNews == null) {
                        skippedNewsCnt++;
                        continue;
                    }
                    savedNewsCnt++;

                    // 3) 성향 저장 (이미 존재하면 upsert)
                    newsTendencyService.saveOrUpdateFromTc(savedNews, nd.getNewsTc());
                    savedTendencyCnt++;
                }

                totalSavedNews += savedNewsCnt;
                totalSavedTendency += savedTendencyCnt;
                totalSkippedNews += skippedNewsCnt;

                logger.info("✅ Batch {} saved: news={}, tendencies={}, skipped={}",
                        batchIdx, savedNewsCnt, savedTendencyCnt, skippedNewsCnt);
            }

            logger.info("🏁 All batches done. totalSavedNews={}, totalSavedTendency={}, totalSkippedNews={}",
                    totalSavedNews, totalSavedTendency, totalSkippedNews);

        } catch (Exception e) {
            logger.error("Error occurred during scheduled crawling: ", e);
        }
    }

    /**
     * OpenAI 에러/비정상 응답이면 분류 스킵하고 뉴스만 저장
     */
    private boolean checkingResponseValidForBatch(String response, List<NewsData> batch, int batchIdx) {
        if (response == null
                || response.startsWith("OpenAI API Error")
                || response.startsWith("Unexpected error")) {

            logger.warn("⚠️ Skip whole batch (batch={}) due to OpenAI error/invalid response: {}",
                    batchIdx, response);

            // ⛔️ 더 이상 뉴스도 저장하지 않는다
            return true;   // 호출부에서 continue;
        }
        return false;
    }

    /**
     * 링크 정규화 + 같은 배치 내 중복 제거
     */
    private List<NewsData> deDuplicateInBatch(List<NewsData> rawNews) {
        Set<String> seen = new HashSet<>();
        return rawNews.stream()
                .peek(nd -> nd.setLink(normalizeLink(nd.getLink())))
                .filter(nd -> seen.add(nd.getLink()))
                .toList();
    }

    /**
     * 쿼리/프래그먼트 제거하여 링크 표준화
     */
    private String normalizeLink(String link) {
        if (link == null) return null;
        try {
            URI uri = new URI(link);
            return new URI(
                    uri.getScheme(),
                    uri.getAuthority(),
                    uri.getPath(),
                    null,  // query 제거
                    null   // fragment 제거
            ).toString();
        } catch (Exception e) {
            return link; // 실패 시 원본 유지
        }
    }
}
