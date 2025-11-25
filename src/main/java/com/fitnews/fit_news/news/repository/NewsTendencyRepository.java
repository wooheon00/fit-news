package com.fitnews.fit_news.news.repository;

import com.fitnews.fit_news.news.entity.News;
import com.fitnews.fit_news.news.entity.NewsTendency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface NewsTendencyRepository extends JpaRepository<NewsTendency, Long> {
    Optional<NewsTendency> findByNews(News news);
    boolean existsByNews_Link(String link);  // ✅ 링크로 성향 존재 여부// ← 추가
    Optional<NewsTendency> findByNewsId(Long newsId);

    // ✅ 여러 뉴스 ID에 해당하는 성향 삭제
    @Modifying
    @Transactional
    @Query("delete from NewsTendency nt where nt.news.id in :newsIds")
    int deleteByNewsIds(@Param("newsIds") List<Long> newsIds);
}
