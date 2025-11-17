package com.fitnews.fit_news.memberPreference.service;

import com.fitnews.fit_news.memberPreference.entity.MemberPreference;
import com.fitnews.fit_news.memberPreference.util.AgeBucket;
import com.fitnews.fit_news.news.entity.NewsTendency;

public class SimilarityCalculator {

    /**
     * 0~100 사이 값 두 개의 거리 기반 유사도
     * 완전히 같으면 1, 완전히 다르면(100 차이) 0
     */
    private static double scoreRange100(int a, int b) {
        return 1.0 - (Math.abs(a - b) / 100.0);
    }

    /**
     * 정치 성향 유사도
     */
    public static double politicSimilarity(MemberPreference pref, NewsTendency news) {
        return scoreRange100(pref.getPolitic(), news.getPolitic());
    }

    /**
     * 성별 성향 유사도
     */
    public static double genderSimilarity(MemberPreference pref, NewsTendency news) {
        return scoreRange100(pref.getGender(), news.getGender());
    }

    /**
     * 나이대 유사도
     * 같은 연령대: 1.0
     * 인접 연령대: 0.5
     * 그 외: 0
     */
    public static double ageSimilarity(MemberPreference pref, NewsTendency news) {
        int u = AgeBucket.toBucket(pref.getAge());
        int n = AgeBucket.toBucket(news.getAge());

        if (u == n) return 1.0;
        if (Math.abs(u - n) == 1) return 0.5;
        return 0.0;
    }

    /**
     * 🔥 종합 유사도 점수 (가중합)
     */
    public static double totalSimilarity(MemberPreference pref, NewsTendency news) {
        double politicScore = politicSimilarity(pref, news);
        double genderScore  = genderSimilarity(pref, news);
        double ageScore     = ageSimilarity(pref, news);

        double wPolitic = 0.5;
        double wGender  = 0.2;
        double wAge     = 0.3;

        return wPolitic * politicScore
                + wGender  * genderScore
                + wAge     * ageScore;
    }
}
