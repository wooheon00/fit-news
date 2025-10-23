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
import java.util.ArrayList;
import java.util.List;

@Service
@Component
public class NewsCrawlingService {
    private static final Logger logger =
            LoggerFactory.getLogger(NewsCrawlingService.class);
    String Base_Url = "https://news-ex.jtbc.co.kr/v1/get/rss/section/";
    static final String[] SECTIONS = {
            "politics",
//            "economy",
//            "society",
//            "international",
//            "culture",
//            "entertaining",
//            "sports",
//            "weather"
    };

    public NewsCrawlingService() throws FeedException, IOException {
    }

    public List<NewsData> crawlingNews(long crawlingTime) throws IOException {
        logger.info("Crawling started. crawlingTime(epoch ms) = {}", crawlingTime);

        List<NewsData> crawledNews = new ArrayList<>();

        for(String section : SECTIONS){
            String feedUrl = Base_Url+section;

            try{
                URL url = new URL(feedUrl);
                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feed = input.build(new XmlReader(url));

                logger.info("Fetching section: {}", section);

                for (SyndEntry entry : feed.getEntries()) {
                    String title = entry.getTitle();
                    String link = entry.getLink();
                    String description = entry.getDescription() != null
                            ? entry.getDescription().getValue()
                            : "No description available";

                    crawledNews.add(new NewsData(title, link, description));
                }
            } catch(Exception e){
                logger.error("Error while fetching section: {}", section, e);
            }
        }

        logger.info("Crawling finished. found {} items", crawledNews.size());
        // TEST
//        System.out.println("\n 뉴스 출력");
//        for(NewsData news : crawledNews){
//            System.out.println(news);
//        }

        return crawledNews;
    }
}
