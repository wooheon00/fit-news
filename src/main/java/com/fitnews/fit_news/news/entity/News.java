package com.fitnews.fit_news.news.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String link;

    @Column(length = 2000)
    private String description;

    private LocalDateTime pubDate;

    // ✅ 썸네일 URL 추가
    private String thumbnailUrl;
}