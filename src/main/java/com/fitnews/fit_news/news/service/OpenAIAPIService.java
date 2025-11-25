package com.fitnews.fit_news.news.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

/*
전처리(프롬프트화)된 뉴스 데이터를 이용, OpenAI API와 통신
 */

@Service
public class OpenAIAPIService {
    private static final Logger logger =
            LoggerFactory.getLogger(OpenAIAPIService.class);

    @Value("${openai.api.key}")
    private String apiKey;

    private WebClient webClient;

    @PostConstruct
    public void init(){
        logger.info("✅ API Key Loaded: " + (apiKey != null ? "YES" : "NO"));

        webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        logger.info("✅ WebClient Initialized, API Key loaded");
    }


    public String askChatGPT(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt cannot be null or blank");
        }

        Map<String, Object> sys = Map.of(
                "role", "system",
                "content", "You are a JSON-only API. Answer with ONLY a JSON array, no prose, no code fences, no explanations."
        );
        Map<String, Object> user = Map.of("role", "user", "content", prompt);

        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(sys, user),
                "temperature", 0.2,
                "max_tokens", 4096      // 🔸 길이 부족으로 끊기는 것 방지
                // 최신 모델이면 response_format(JSON 강제) 옵션 고려
        );

        logger.info("➡️ Sending request to OpenAI");

        try {
            Map<String, Object> response = webClient.post()
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !response.containsKey("choices")) {
                return "OpenAI API Error: empty response";
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices.isEmpty()) return "OpenAI API Error: no choices";

            Map<String, Object> messageMap = (Map<String, Object>) choices.get(0).get("message");
            if (messageMap == null || !messageMap.containsKey("content"))
                return "OpenAI API Error: no content";

            String content = (String) messageMap.get("content");
            // 디버그: 앞 1k자만
            logger.debug("Raw GPT content (preview): {}", content != null && content.length() > 1000 ? content.substring(0,1000)+"..." : content);
            return content;

        } catch (WebClientResponseException e) {
            logger.error("OpenAI API Error: {} - {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            return "OpenAI API Error: " + e.getRawStatusCode();
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            return "Unexpected error occurred";
        }
    }

}