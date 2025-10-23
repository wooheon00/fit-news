package com.fitnews.fit_news.news.service;

/*
추천 점수 척도 : UserTC와 NewsTC간의 거리
추천 점수 기준 내림차순 정렬 후 상위 N개 반환
 */

import com.fitnews.fit_news.news.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fitnews.fit_news.news.model.NewsData;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class NewsRecommendationService {
    private static final Logger logger =
            LoggerFactory.getLogger(NewsRecommendationService.class);

    public List<NewsData> createNewsReco(List<NewsData> newsData, UserInfo userInfo){
        if (newsData == null || newsData.isEmpty()) return Collections.emptyList();
        if (userInfo == null) {
            // 사용자 정보가 없으면 최신 뉴스(최대 10개) 반환
            return newsData.stream().limit(10).toList();
        }

        // TODO 점수 계산

        // TODO 정렬: 점수 내림차순

        //logger.info("Created {} recommendations for user {}", result.size(), userInfo.getUserId());
        return null;
    }

}
