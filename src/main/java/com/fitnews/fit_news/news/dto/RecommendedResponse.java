package com.fitnews.fit_news.news.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RecommendedResponse {
    private List<NewsDto> aligned;
    private List<NewsDto> opposite;

    public static RecommendedResponse from(RecommendedResult result) {
        return new RecommendedResponse(
                result.getAligned().stream().map(NewsDto::from).toList(),
                result.getOpposite().stream().map(NewsDto::from).toList()
        );
    }
}
