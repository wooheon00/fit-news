package com.fitnews.fit_news.news.model;

/*
Tendency 클래스
 */

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)        // 알 수 없는 필드는 무시
public class Tc {
    private int index;

    @JsonAlias({"zender", "gender"})               // ← 둘 다 허용
    private int gender;

    private int politic;
    private char age;
}
