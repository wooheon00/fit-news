package com.fitnews.fit_news.log.repository;

import com.fitnews.fit_news.log.entity.ClickLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClickLogRepository extends JpaRepository<ClickLog, Long> {
}
