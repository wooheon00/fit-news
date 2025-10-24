package com.fitnews.fit_news.news.repository;

import com.fitnews.fit_news.news.entity.News;
import com.fitnews.fit_news.news.entity.NewsTendency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NewsTendencyRepository extends JpaRepository<NewsTendency, Long> {
    Optional<NewsTendency> findByNews(News news);
    boolean existsByNews_Link(String link);  // ✅ 링크로 성향 존재 여부// ← 추가
}
