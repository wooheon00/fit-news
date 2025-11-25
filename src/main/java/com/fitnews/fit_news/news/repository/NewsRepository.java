package com.fitnews.fit_news.news.repository;

import com.fitnews.fit_news.news.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NewsRepository extends JpaRepository<News, Long> {
    Optional<News> findByLink(String link);
    boolean existsByLink(String link);// ✅ 중복 체크용

    @Query("""
        SELECT n
        FROM News n
        WHERE n.id NOT IN (
            SELECT c.newsId
            FROM ClickLog c
            WHERE c.userId = :memberId
        )
        ORDER BY n.pubDate DESC
        """)
    List<News> findRecentNotClickedByMember(@Param("memberId") Long memberId);

    // ✅ 1주일 지난 뉴스 ID 조회용
    @Query("select n.id from News n where n.pubDate < :cutoff")
    List<Long> findIdsByPubDateBefore(@Param("cutoff") LocalDateTime cutoff);

    // ✅ ID 목록으로 뉴스 삭제
    @Modifying
    @Transactional
    @Query("delete from News n where n.id in :ids")
    int deleteByIdIn(@Param("ids") List<Long> ids);
}
