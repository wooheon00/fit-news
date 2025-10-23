package com.fitnews.fit_news.news.model;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/*
뉴스 도메인에서 사용할 사용자 정보 DTO
 */
@Getter
@Setter
@RequiredArgsConstructor
public class UserInfo {
    private Long userId;
    private String username;
    private Tc userTc;
}
