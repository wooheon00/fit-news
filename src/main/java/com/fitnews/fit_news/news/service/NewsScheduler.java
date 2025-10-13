package com.fitnews.fit_news.news.service;

import com.fitnews.fit_news.news.model.NewsData;
import com.fitnews.fit_news.news.repository.NewsRepository;
import com.fitnews.fit_news.news.repository.NewsTendencyRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NewsScheduler {
    private static final Logger logger
            = LoggerFactory.getLogger(NewsScheduler.class);

    private final NewsCrawlingService newsCrawlingService;
    private final OpenAIAPIService openAIAPIService;
    private final NewsClassificationService newsClassificationService;
    // 레포지토리 선언
    private static NewsRepository newsRepository;
    private static NewsTendencyRepository newsTendencyRepository;

    /*
    fixedRate = 60000 -> 이전 시행 후 60초마다 시행(서버시간 기준)
     */
    @Scheduled(fixedRate = 60_000)
    public void runCrawlingJob(){
        long crawlingTime = Instant.now().toEpochMilli();
        logger.info("NewsScheduler triggered at {}", crawlingTime);

        try{
            //1 Crawling
            List<NewsData> rawNews = newsCrawlingService.crawlingNews(crawlingTime);

            if(rawNews==null || rawNews.isEmpty()){
                logger.info("No new news items found at {}", crawlingTime);
                return;
            }

            //2 Classification
            List<NewsData> classifiedNews
                    = newsClassificationService.postprocessingNews(
                    openAIAPIService.askChatGPT(newsClassificationService.preprocessingNews(rawNews)),
                    rawNews
            );

            //3 Saving
            // TODO : 전처리된 뉴스 저장

            logger.info("Saved {} news items from crawling at {}", classifiedNews.size(), crawlingTime);

        } catch(Exception e){
            logger.error("Error occurred during scheduled crawling: ", e);
        }
    }
}
