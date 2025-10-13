package com.fitnews.fit_news.news;


import com.fitnews.fit_news.news.service.NewsScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
public class NewsSchedulerTest {
    @Autowired
    private NewsScheduler newsScheduler;

    @Test
    @DisplayName("뉴스 크롤링 로직 : 주기적 크롤링 -> 전처리 -> OpenAI API 요청 -> 후처리 -> 저장 테스트")
    void testNewsSchedulerLogic() throws IOException{
        newsScheduler.runCrawlingJob();
    }

}
