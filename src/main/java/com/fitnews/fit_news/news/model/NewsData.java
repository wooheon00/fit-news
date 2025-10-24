package com.fitnews.fit_news.news.model;

import com.fitnews.fit_news.news.entity.News;
import com.fitnews.fit_news.news.entity.NewsTendency;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class NewsData {
    private String title;
    private String link;
    private String description;

    private java.time.LocalDateTime pubDate;   // 🔹 추가
    private Tc newsTc = null;
    private Boolean isClassified = false;

    public NewsData(String title, String link, String description, java.time.LocalDateTime pubDate) {
        this.title = title;
        this.link = link;
        this.description = description;
        this.pubDate = pubDate;  // 🔹 설정
    }

    public void setTc(Tc newsTc) { this.newsTc = newsTc; }

    @Override
    public String toString() {
        if (newsTc != null)
            return title + description + newsTc.getAge() + newsTc.getGender() + newsTc.getPolitic();
        return title + description;
    }

    // =====================================================
    // 🔽 🔽 변환 메서드 추가 (DTO → Entity)
    // =====================================================

    /**
     * DTO → News 엔티티 변환
     */
    public News toNewsEntity() {
        News news = new News();
        news.setTitle(this.title);
        news.setLink(this.link);
        news.setDescription(this.description);
        news.setPubDate(this.pubDate);
        return news;
    }

    /**
     * DTO + Tc → NewsTendency 엔티티 변환
     * (News 엔티티가 이미 저장된 상태에서 호출)
     */
    public NewsTendency toTendencyEntity(News newsEntity) {
        if (this.newsTc == null) return null; // 분류되지 않은 경우
        NewsTendency tendency = new NewsTendency();
        tendency.setNews(newsEntity);
        tendency.setGender(this.newsTc.getGender());
        tendency.setPolitic(this.newsTc.getPolitic());
        tendency.setAge(this.newsTc.getAge());
        return tendency;
    }
}

