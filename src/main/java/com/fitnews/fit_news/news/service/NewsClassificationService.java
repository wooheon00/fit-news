package com.fitnews.fit_news.news.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fitnews.fit_news.news.model.NewsData;
import com.fitnews.fit_news.news.model.Tc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/*
preprocessingNews : 크롤러에서 넘어온 raw news를 전처리(프롬프트화)
postprocessingNews : OpenAI API에서 넘어온 news를 후처리(NewsData화)

입/출력 : List<NewsData>
 */

@Service
public class NewsClassificationService {
    private static final Logger logger =
            LoggerFactory.getLogger(NewsClassificationService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 전처리
    String preprocessingNews(List<NewsData> newsData){
        if (newsData == null) return "";
        logger.info("Preprocessing {} news items", newsData.size());

        // TODO 전처리 로직 : Build Prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
        You are an AI model that classifies news articles based on three attributes:
        1. zender (0-100): 0 means female-oriented, 100 means male-oriented.
        2. politic (0-100): 0 means conservative, 100 means progressive.
        3. age (a/b/c/d): a=minor, b=young adult, c=middle-aged, d=senior.

        ⚠️ Respond ONLY in this JSON array format:
        [
          {"index":1, "zender":int, "politic":int, "age":"a|b|c|d"},
          {"index":2, "zender":int, "politic":int, "age":"a|b|c|d"}
        ]

        Below are the news items to classify:
        """);

        int index=1;
        for (NewsData news : newsData) {
            prompt.append(String.format("""
                
                [%d]
                Title: %s
                Description: %s
                """,
                    index++,
                    news.getTitle(),
                    news.getDescription() != null ? news.getDescription() : "No description"));
        }

        prompt.append("\nNow, provide classification results ONLY in JSON array.\n");

        logger.info("After Preprocessing {} news items", newsData.size());
        return prompt.toString();
    }

    /**
     * GPT 응답(JSON)을 기반으로 각 NewsData에 Classification 결과(Tc)를 주입
     *
     * @param response GPT 응답 문자열 (JSON Array 형식)
     * @param newsData 분류 대상 뉴스 목록
     * @return Tc 정보가 포함된 뉴스 리스트
     */
    List<NewsData> postprocessingNews(String response, List<NewsData> newsData){
        if (newsData == null) return Collections.emptyList();
        logger.info("Postprocessing {} news items", newsData.size());

        // TODO 후처리 로직
        try{
            // GPT 응답을 List<Tc> 형태로 파싱
            List<Tc> classifications = objectMapper.readValue(response, new TypeReference<List<Tc>>() {});

            // 뉴스 개수와 분류 결과 개수가 동일할 때 매핑
            for (int i = 0; i < newsData.size() && i < classifications.size(); i++) {
                newsData.get(i).setNewsTc(classifications.get(i));
            }

            logger.info("After Postprocessing {} news items", newsData.size());

            return newsData;
        } catch (Exception e){
            throw new RuntimeException("❌ Classification 파싱 실패: " + e.getMessage(), e);
        }

    }
}
