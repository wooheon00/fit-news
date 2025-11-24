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
import java.util.Map;

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

        // BuildPrompt
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
    public List<NewsData> postprocessingNews(String response, List<NewsData> newsData) {
        if (newsData == null) return Collections.emptyList();
        logger.info("Postprocessing {} news items", newsData.size());

        try {
            // OpenAI 에러/빈값 방어
            if (response == null
                    || response.startsWith("OpenAI API Error")
                    || response.startsWith("Unexpected error")) {
                logger.warn("Skip postprocessing due to OpenAI error: {}", response);
                return newsData; // 분류 없이 통과 (죽지 않도록)
            }

            // 🔐 JSON 배열만 추출 (미완/설명/코드펜스 제거)
            String json = extractBalancedJsonArray(response);
            if (json == null) {
                // 원문 일부만 로그 (너무 길면 1k자 제한)
                String preview = response.length() > 1000 ? response.substring(0, 1000) + "..." : response;
                logger.error("❌ GPT 응답에서 JSON 배열을 추출하지 못했습니다. preview=\n{}", preview);
                return newsData; // 죽지 않고 넘어감
            }

            List<Tc> tcList = objectMapper.readValue(json, new TypeReference<List<Tc>>() {
            });
            for (Tc tc : tcList) {
                int index = tc.getIndex() - 1;
                if (0 <= index && index < newsData.size()) {
                    newsData.get(index).setTc(tc);
                } else {
                    logger.warn("Invalid index {} for newsData size {}", tc.getIndex(), newsData.size());
                }
            }

            logger.info("After Postprocessing {} news items", newsData.size());
            return newsData;

        } catch (Exception e) {
            // 원문 일부 덤프
            String preview = response != null
                    ? (response.length() > 1000 ? response.substring(0, 1000) + "..." : response)
                    : "null";
            logger.error("❌ Classification 파싱 실패. preview=\n{}", preview, e);
            return newsData; // 여기서도 죽지 말고 통과
        }
    }

    private String extractBalancedJsonArray(String s) {
        if (s == null) return null;

        // 1) 코드펜스/잡말 제거
        String cleaned = s
                .replace("```json", "")
                .replace("```", "")
                .trim();

        // 2) 배열의 시작 '[' 위치 탐색
        int start = cleaned.indexOf('[');
        if (start < 0) return null;

        // 3) 괄호 밸런싱으로 배열 끝 위치 찾기
        int depth = 0;
        boolean inString = false;
        char prev = 0;
        for (int i = start; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            if (c == '"' && prev != '\\') inString = !inString;
            if (inString) { prev = c; continue; }

            if (c == '[') depth++;
            else if (c == ']') {
                depth--;
                if (depth == 0) {
                    return cleaned.substring(start, i + 1);
                }
            }
            prev = c;
        }
        // 못 찾으면 null
        return null;
    }

    public String detectSourceFromLink(String link){
        if (link==null) return "Unknown";

        String lower = link.toLowerCase();

        if (lower.contains("jtbc")) return "JTBC";
        if (lower.contains("yna")) return "연합뉴스";
        if (lower.contains("chosun")) return "조선일보";
        if (lower.contains("mk")) return "매일경제";

        return "Unknown";
    }
}
