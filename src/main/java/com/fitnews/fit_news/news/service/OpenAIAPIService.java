package com.fitnews.fit_news.news.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/*
전처리(프롬프트화)된 뉴스 데이터를 이용, OpenAI API와 통신
 */

@Service
public class OpenAIAPIService {
    private static final Logger logger =
            LoggerFactory.getLogger(OpenAIAPIService.class);

    // TODO request method
    private String apiKey = "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";

    @PostConstruct
    public void init(){
        System.out.println("✅ API Key Loaded: " + (apiKey != null ? "YES" : "NO"));
    }

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.openai.com/v1/chat/completions")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    public String askChatGPT(String prompt) {
        Map<String, Object> message = Map.of("role", "user", "content", prompt);
        Map<String, Object> body = Map.of(
                "model", "gpt-4",
                "messages", List.of(message),
                "temperature", 0.7
        );

        return webClient.post()
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> messageMap = (Map<String, Object>) choice.get("message");
                    return (String) messageMap.get("content");
                })
                .block();
    }
}
