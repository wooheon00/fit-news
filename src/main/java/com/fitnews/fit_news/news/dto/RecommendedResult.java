package com.fitnews.fit_news.news.dto;

import com.fitnews.fit_news.news.entity.News;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RecommendedResult {
    private List<News> aligned;
    private List<News> opposite;
}
