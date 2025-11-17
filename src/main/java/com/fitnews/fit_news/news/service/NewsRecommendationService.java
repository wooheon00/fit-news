package com.fitnews.fit_news.news.service;

/*
추천 점수 척도 : UserTC와 NewsTC간의 거리
추천 점수 기준 내림차순 정렬 후 상위 N개 반환
 */

import com.fitnews.fit_news.log.repository.ClickLogRepository;
import com.fitnews.fit_news.memberPreference.entity.MemberPreference;
import com.fitnews.fit_news.memberPreference.repository.MemberPreferenceRepository;
import com.fitnews.fit_news.memberPreference.service.SimilarityCalculator;
import com.fitnews.fit_news.news.entity.News;
import com.fitnews.fit_news.news.entity.NewsTendency;
import com.fitnews.fit_news.news.model.UserInfo;
import com.fitnews.fit_news.news.repository.NewsRepository;
import com.fitnews.fit_news.news.repository.NewsTendencyRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fitnews.fit_news.news.model.NewsData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsRecommendationService {

    private final MemberPreferenceRepository memberPreferenceRepository;
    private final NewsRepository newsRepository;
    private final NewsTendencyRepository newsTendencyRepository;
    private final ClickLogRepository clickLogRepository;

    /**
     * 🔥 회원에게 상위 N개 뉴스 추천
     */
    @Transactional(readOnly = true)
    public List<News> recommend(Long memberId, int limit) {

        // 1) 회원 취향 가져오기
        MemberPreference pref = memberPreferenceRepository.findByMember_Id(memberId)
                .orElseThrow(() -> new IllegalStateException("MemberPreference 없음"));

        // 2) 아직 안 본 뉴스 후보 가져오기 (예: 최근 3일치)
        List<News> candidates = newsRepository.findRecentNotClickedByMember(memberId);

        // 3) 각 뉴스에 대해 유사도 점수 계산
        List<ScoredNews> scored = new ArrayList<>();

        for (News news : candidates) {
            NewsTendency tendency = newsTendencyRepository.findByNewsId(news.getId())
                    .orElse(null);

            if (tendency == null) {
                // 성향 정보 없는 뉴스는 일단 스킵하거나 기본 점수 부여
                continue;
            }

            double similarity = SimilarityCalculator.totalSimilarity(pref, tendency);

            // (선택) 발행일 기반 신선도 점수 추가
            double recency = calcRecencyScore(news.getPubDate());

            double totalScore = 0.8 * similarity + 0.2 * recency;

            scored.add(new ScoredNews(news, totalScore));
        }

        // 4) 점수 순으로 정렬 후 상위 N개 리턴
        return scored.stream()
                .sorted((a, b) -> Double.compare(b.score, a.score)) // 내림차순
                .limit(limit)
                .map(s -> s.news)
                .toList();
    }

    /**
     * 단순 예시: 최근 1일 이내면 1.0, 3일 이내면 0.7, 7일 이내면 0.4, 그 외 0.1
     */
    private double calcRecencyScore(LocalDateTime pubDate) {
        if (pubDate == null) return 0.0;

        long days = Duration.between(pubDate, LocalDateTime.now()).toDays();

        if (days <= 1) return 1.0;
        if (days <= 3) return 0.7;
        if (days <= 7) return 0.4;
        return 0.1;
    }

    /**
     * 내부용 DTO
     */
    private static class ScoredNews {
        private final News news;
        private final double score;

        private ScoredNews(News news, double score) {
            this.news = news;
            this.score = score;
        }
    }
}
