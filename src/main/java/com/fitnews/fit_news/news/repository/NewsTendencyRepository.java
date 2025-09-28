package com.fitnews.fit_news.news.repository;

import com.fitnews.fit_news.news.entity.NewsTendency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsTendencyRepository extends JpaRepository<NewsTendency, Long> {
}
