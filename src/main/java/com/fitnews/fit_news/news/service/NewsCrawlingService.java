package com.fitnews.fit_news.news.service;

import com.fitnews.fit_news.news.model.NewsData;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service // ✅ @Component는 제거 (중복 방지)
public class NewsCrawlingService {
    private static final Logger logger = LoggerFactory.getLogger(NewsCrawlingService.class);
    private static final String BASE_URL = "https://news-ex.jtbc.co.kr/v1/get/rss/section/";
    static final String[] SECTIONS = { "politics" /*, "economy", ... */ };

    public NewsCrawlingService() { } // ✅ throws 제거

    public List<NewsData> crawlingNews(long crawlingTime) {
        logger.info("Crawling started. crawlingTime(epoch ms) = {}", crawlingTime);
        List<NewsData> crawledNews = new ArrayList<>();

        for (String section : SECTIONS) {
            String feedUrl = BASE_URL + section;
            try (XmlReader reader = new XmlReader(new URL(feedUrl))) {
                SyndFeed feed = new SyndFeedInput().build(reader);
                logger.info("Fetching section: {}", section);

                for (SyndEntry entry : feed.getEntries()) {
                    String title = entry.getTitle();
                    String link = entry.getLink();
                    String description = entry.getDescription() != null
                            ? entry.getDescription().getValue()
                            : "No description available";

                    // 🔹 pubDate 변환
                    java.util.Date published = entry.getPublishedDate(); // null일 수도 있음
                    java.time.LocalDateTime pubDate = (published != null)
                            ? java.time.ZonedDateTime.ofInstant(published.toInstant(), java.time.ZoneId.systemDefault())
                            .toLocalDateTime()
                            : java.time.LocalDateTime.now();

                    crawledNews.add(new NewsData(title, link, description, pubDate)); // 🔹 4개 인자 사용
                }
            } catch (Exception e) {
                logger.error("Error while fetching section: {}", section, e);
            }
        }

        logger.info("Crawling finished. found {} items", crawledNews.size());
        return crawledNews;
    }
}
