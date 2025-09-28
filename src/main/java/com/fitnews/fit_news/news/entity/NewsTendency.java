package com.fitnews.fit_news.news.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class NewsTendency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "news_id", nullable = false)
    private News news;

    private String targetAge;    // 예: "20대", "30대"
    private String targetGender; // 예: "M", "F"
}
