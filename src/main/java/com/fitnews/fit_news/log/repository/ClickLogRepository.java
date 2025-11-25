package com.fitnews.fit_news.log.repository;

import com.fitnews.fit_news.log.entity.ClickLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ClickLogRepository extends JpaRepository<ClickLog, Long> {

    // ✅ 여러 뉴스 ID에 해당하는 로그 삭제
    @Modifying
    @Transactional
    @Query("delete from ClickLog c where c.newsId in :newsIds")
    int deleteByNewsIds(@Param("newsIds") List<Long> newsIds);

}
