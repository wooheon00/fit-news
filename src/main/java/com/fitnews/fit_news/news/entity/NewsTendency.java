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

    // 🔹 어떤 뉴스의 성향인지 연결
    @OneToOne
    @JoinColumn(name = "news_id", nullable = false, unique = true)
    private News news;

    // 🔹 성별 지향성 (0=여성향, 100=남성향)
    @Column(nullable = false)
    private int gender;

    // 🔹 정치 성향 (0=보수, 100=진보)
    @Column(nullable = false)
    private int politic;

    // 🔹 연령대 (a=미성년자, b=청년, c=중년, d=노년)
    @Column(length = 1, nullable = false)
    private char age;
}
