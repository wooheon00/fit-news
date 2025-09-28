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

    public News saveNews(News news) {
        return newsRepository.findByLink(news.getLink())
                .orElseGet(() -> {
                    // ✅ link에서 썸네일 크롤링
                    String ogImage = OgImageFetcher.getOgImage(news.getLink());
                    if (ogImage != null) {
                        news.setThumbnailUrl(ogImage);
                    } else {
                        news.setThumbnailUrl("/images/default-thumb.png"); // fallback
                    }

                    return newsRepository.save(news);
                });
    }

    public List<News> getAllNews() {
        return newsRepository.findAll();
    }
}
