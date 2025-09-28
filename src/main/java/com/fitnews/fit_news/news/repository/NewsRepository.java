package com.fitnews.fit_news.news.repository;

import com.fitnews.fit_news.news.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NewsRepository extends JpaRepository<News, Long> {
    Optional<News> findByLink(String link);
}
