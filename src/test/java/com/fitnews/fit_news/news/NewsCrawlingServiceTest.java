package com.fitnews.fit_news.news;

import com.fitnews.fit_news.news.service.NewsCrawlingService;
import com.fitnews.fit_news.news.model.NewsData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class NewsCrawlingServiceTest {
    @Autowired
    private NewsCrawlingService newsCrawlingService;

    @Test
    @DisplayName("JTBC 뉴스 크롤링 정상 동작 테스트")
    void testNewsCrawlingService() throws IOException{
        List<NewsData> newsList = newsCrawlingService.crawlingNews(0);

        // 크롤링 결과가 비어 있지 않은지 확인
        assertNotNull(newsList, "크롤링 결과가 null이면 안 됨");
        assertFalse(newsList.isEmpty(), "뉴스 목록이 비어 있으면 안 됨");

        // 최소한의 필드 유효성 검사
        NewsData sample = newsList.get(0);
        assertNotNull(sample.getTitle(), "뉴스 제목은 null이면 안 됨");
        assertTrue(sample.getLink().startsWith("https://news.jtbc.co.kr"), "링크 형식이 올바르지 않음");

        // 디버그 출력
        System.out.println("✅ 수집된 뉴스 개수: " + newsList.size());
        System.out.println("예시 뉴스 제목: " + sample.getTitle());
        System.out.println("예시 링크: " + sample.getLink());
    }

}
