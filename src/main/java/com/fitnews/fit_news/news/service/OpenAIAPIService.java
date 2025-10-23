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

        Map<String, Object> message = Map.of("role", "user", "content", prompt);
        Map<String, Object> body = Map.of(
                "model", "gpt-4",
                "messages", List.of(message),
                "temperature", 0.7
        );

        logger.info("➡️ Sending request to OpenAI: {}", body);

        try {
            // 3. WebClient 요청
            Map<String, Object> response = webClient.post()
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            logger.info("⬅️ OpenAI response: {}", response);

            // 4. 안전한 응답 처리
            if (response == null || !response.containsKey("choices")) {
                return "No response from OpenAI";
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices.isEmpty()) return "No choices returned";

            Map<String, Object> messageMap = (Map<String, Object>) choices.get(0).get("message");
            if (messageMap == null || !messageMap.containsKey("content")) return "No content returned";

            return (String) messageMap.get("content");

        } catch (WebClientResponseException e) {
            logger.error("OpenAI API Error: {} - {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            return "OpenAI API Error: " + e.getRawStatusCode();
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            return "Unexpected error occurred";
        }
//        return webClient.post()
//                .bodyValue(body)
//                .retrieve()
//                .bodyToMono(Map.class)
//                .map(response -> {
//                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
//                    Map<String, Object> choice = choices.get(0);
//                    Map<String, Object> messageMap = (Map<String, Object>) choice.get("message");
//                    return (String) messageMap.get("content");
//                })
//                .block();
    }
}