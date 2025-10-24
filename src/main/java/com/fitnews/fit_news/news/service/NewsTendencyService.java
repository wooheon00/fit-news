package com.fitnews.fit_news.news.service;

import com.fitnews.fit_news.news.entity.News;
import com.fitnews.fit_news.news.entity.NewsTendency;
import com.fitnews.fit_news.news.model.Tc;
import com.fitnews.fit_news.news.repository.NewsTendencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NewsTendencyService {

    private final NewsTendencyRepository newsTendencyRepository;

    /**
     * 분류 결과 Tc를 기반으로 NewsTendency upsert
     */
    public NewsTendency saveOrUpdateFromTc(News news, Tc tc) {
        if (news == null || tc == null) return null;

        NewsTendency tendency = newsTendencyRepository.findByNews(news)
                .orElseGet(NewsTendency::new);

        tendency.setNews(news);
        tendency.setGender(tc.getGender());
        tendency.setPolitic(tc.getPolitic());
        tendency.setAge(tc.getAge());

        return newsTendencyRepository.save(tendency);
    }
}