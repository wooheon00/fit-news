package com.fitnews.fit_news.news.repository;


import com.fitnews.fit_news.news.entity.News;
import com.fitnews.fit_news.news.util.OgImageFetcher;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class NewsDataLoader implements CommandLineRunner {

    private final NewsRepository newsRepository;

    @Override
    public void run(String... args) throws Exception {

    }

    private void saveIfNotExists(String title, String link, String description, LocalDateTime pubDate) {
        newsRepository.findByLink(link).orElseGet(() -> {
            News news = new News();
            news.setTitle(title);
            news.setLink(link);
            news.setDescription(description);
            news.setPubDate(pubDate);

            // ✅ 썸네일 자동 추출
            String ogImage = OgImageFetcher.getOgImage(link);
            if (ogImage != null) {
                news.setThumbnailUrl(ogImage);
            } else {
                news.setThumbnailUrl("/images/default-thumb.png");
            }

            return newsRepository.save(news);
        });
    }
}