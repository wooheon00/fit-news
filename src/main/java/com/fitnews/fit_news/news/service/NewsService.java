package com.fitnews.fit_news.news.service;

import com.fitnews.fit_news.news.entity.News;
import com.fitnews.fit_news.news.repository.NewsRepository;
import com.fitnews.fit_news.news.util.OgImageFetcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsService {
    private final NewsRepository newsRepository;

    /** 이미 있으면 false, 새로 저장되면 true를 반환 */
    public boolean saveIfNew(News news) {
        if (newsRepository.findByLink(news.getLink()).isPresent()) {
            return false;
        }
        String ogImage = OgImageFetcher.getOgImage(news.getLink());
        news.setThumbnailUrl(ogImage != null ? ogImage : "/images/default-thumb.png");
        newsRepository.save(news);
        return true;
    }

    public News saveNews(News news) {
        // 필요하면 기존 메서드도 유지
        return newsRepository.findByLink(news.getLink())
                .orElseGet(() -> {
                    String ogImage = OgImageFetcher.getOgImage(news.getLink());
                    news.setThumbnailUrl(ogImage != null ? ogImage : "/images/default-thumb.png");
                    return newsRepository.save(news);
                });
    }

    public List<News> getAllNews() { return newsRepository.findAll(); }
}
