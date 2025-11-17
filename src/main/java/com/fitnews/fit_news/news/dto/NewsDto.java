package com.fitnews.fit_news.news.dto;

import com.fitnews.fit_news.news.entity.News;

public record NewsDto(
        Long id,
        String title,
        String link,
        String description,
        String thumbnailUrl
) {
    public static NewsDto from(News n) {
        return new NewsDto(
                n.getId(),
                n.getTitle(),
                n.getLink(),
                n.getDescription(),
                n.getThumbnailUrl()
        );
    }
}

