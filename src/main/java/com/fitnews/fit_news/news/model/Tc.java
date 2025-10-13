package com.fitnews.fit_news.news.model;

/*
Tendency 클래스
 */

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Tc {
    // 성별 지향성 (0=여성향, 100=남성향)
    private int zender=50;

    // 정치 성향 (0=보수, 100=진보)
    private int politic=50;

    // 연령대 (a=미성년자, b=청년, c=중년, d=노년)
    private char age='b';
}
