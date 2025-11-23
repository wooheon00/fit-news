package com.fitnews.fit_news.news.service;

/*
추천 점수 척도 : UserTC와 NewsTC간의 거리
추천 점수 기준 내림차순 정렬 후 상위 N개 반환
 */

import com.fitnews.fit_news.log.repository.ClickLogRepository;
import com.fitnews.fit_news.memberPreference.entity.MemberPreference;
import com.fitnews.fit_news.memberPreference.repository.MemberPreferenceRepository;
import com.fitnews.fit_news.memberPreference.service.SimilarityCalculator;
import com.fitnews.fit_news.news.dto.RecommendedResult;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsRecommendationService {

    private final MemberPreferenceRepository memberPreferenceRepository;
    private final NewsRepository newsRepository;
    private final NewsTendencyRepository newsTendencyRepository;
    private final ClickLogRepository clickLogRepository;

    @Transactional(readOnly = true)
    public RecommendedResult recommendWithOpposite(Long memberId, int alignedLimit, int oppositeLimit) {

        // 1) 회원 취향
        MemberPreference pref = memberPreferenceRepository.findByMember_Id(memberId)
                .orElseThrow(() -> new IllegalStateException("MemberPreference 없음"));

        // 2) 추천 후보
        List<News> candidates = newsRepository.findRecentNotClickedByMember(memberId);

        // 3) 유사도 계산
        List<ScoredNews> scored = new ArrayList<>();

        for (News news : candidates) {
            NewsTendency tendency = newsTendencyRepository.findByNewsId(news.getId())
                    .orElse(null);

            if (tendency == null) continue;

            double similarity = SimilarityCalculator.totalSimilarity(pref, tendency);
            double recency = calcRecencyScore(news.getPubDate());
            double totalScore = 0.8 * similarity + 0.2 * recency;

            scored.add(new ScoredNews(news, totalScore, similarity));
        }

        // ---------------------
        // 4) 정렬 후 "나와 맞는 기사 N개"
        // ---------------------
        List<News> aligned = scored.stream()
                .sorted((a, b) -> Double.compare(b.score, a.score)) // 점수 내림차순
                .limit(alignedLimit)
                .map(s -> s.news)
                .toList();

        // ---------------------
        // 5) "나와 가장 반대 성향인 기사 M개"
        //    similarity 오름차순 정렬로 선정
        // ---------------------
        Set<Long> usedIds = aligned.stream()
                .map(News::getId)
                .collect(Collectors.toSet());

        List<News> opposite = scored.stream()
                .filter(s -> !usedIds.contains(s.news.getId()))
                .sorted(Comparator.comparingDouble(s -> s.similarity)) // similarity 오름차순
                .limit(oppositeLimit)
                .map(s -> s.news)
                .toList();

        return new RecommendedResult(aligned, opposite);
    }

    private static class ScoredNews {
        private final News news;
        private final double score;
        private final double similarity;

        public ScoredNews(News news, double score, double similarity) {
            this.news = news;
            this.score = score;
            this.similarity = similarity;
        }
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



}
