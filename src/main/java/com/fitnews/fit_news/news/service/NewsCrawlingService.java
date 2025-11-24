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
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class NewsCrawlingService {

    private static final Logger logger = LoggerFactory.getLogger(NewsCrawlingService.class);

    // 🔹 여러 뉴스 사이트의 RSS URL 목록 (원한다면 외부 properties 파일로 분리 가능)
    private static final Map<String, String[]> NEWS_SOURCES = Map.of(

            "JTBC", new String[]{
                    "https://news-ex.jtbc.co.kr/v1/get/rss/section/politics"
            },

            "조선일보", new String[]{
                    "https://www.chosun.com/arc/outboundfeeds/rss/category/politics/?outputType=xml"
            },

            "연합뉴스", new String[]{
                    "https://www.yna.co.kr/rss/politics.xml"
            },

            "매일경제", new String[]{
                    "https://www.mk.co.kr/rss/30100041/"
            }
    );

    public List<NewsData> crawlingNews(long crawlingTime) {
        logger.info("Crawling started. crawlingTime(epoch ms) = {}", crawlingTime);

        // 1️⃣ 각 뉴스사별 RSS feed 리스트 읽어서 SyndEntry 리스트로 저장
        Map<String, List<SyndEntry>> allFeeds = new HashMap<>();

        for (Map.Entry<String, String[]> siteEntry : NEWS_SOURCES.entrySet()) {
            String siteName = siteEntry.getKey();
            String[] rssUrls = siteEntry.getValue();

            List<SyndEntry> siteEntries = new ArrayList<>();

            for (String rssUrl : rssUrls) {
                try (XmlReader reader = new XmlReader(new URL(rssUrl))) {
                    SyndFeed feed = new SyndFeedInput().build(reader);
                    siteEntries.addAll(feed.getEntries());
                } catch (Exception e) {
                    logger.error("Error fetching RSS {} from {}", rssUrl, siteName, e);
                }
            }

            allFeeds.put(siteName, siteEntries);
        }

        // 2️⃣ 인덱스로 각 뉴스사에서 한 뉴스씩 가져오기
        List<NewsData> crawledNews = new ArrayList<>();
        boolean hasMore = true;
        int index = 0;

        while (hasMore) {
            hasMore = false;

            for (Map.Entry<String, List<SyndEntry>> entry : allFeeds.entrySet()) {
                List<SyndEntry> entries = entry.getValue();

                if (index < entries.size()) {
                    SyndEntry syndEntry = entries.get(index);

                    String title = syndEntry.getTitle();
                    String link = syndEntry.getLink();
                    String description = syndEntry.getDescription() != null
                            ? syndEntry.getDescription().getValue()
                            : "No description available";

                    Date published = syndEntry.getPublishedDate();
                    LocalDateTime pubDate = (published != null)
                            ? ZonedDateTime.ofInstant(published.toInstant(), ZoneId.systemDefault()).toLocalDateTime()
                            : LocalDateTime.now();

                    crawledNews.add(new NewsData(title, link, description, pubDate));

                    hasMore = true;
                }
            }
            index++;
        }

        logger.info("Crawling finished. total collected = {}", crawledNews.size());
        return crawledNews;
    }
}