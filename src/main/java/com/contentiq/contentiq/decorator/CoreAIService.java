package com.contentiq.contentiq.decorator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Slf4j
public class CoreAIService implements AIService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final int maxTokens;
    private final String apiUrl;

    public CoreAIService(
            @Value("${claude.api.key}") String apiKey,
            @Value("${claude.api.url:https://api.anthropic.com/v1/messages}") String apiUrl,
            @Value("${claude.api.model:claude-sonnet-4-20250514}") String model,
            @Value("${claude.api.max-tokens:4096}") int maxTokens,
            @Value("${claude.api.version:2023-06-01}") String anthropicVersion) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.model = model;
        this.maxTokens = maxTokens;
        this.objectMapper = new ObjectMapper();
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", anthropicVersion)
                .build();
    }

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        try {
            ObjectNode request = objectMapper.createObjectNode();
            request.put("model", model);
            request.put("max_tokens", maxTokens);

            if (systemPrompt != null && !systemPrompt.isBlank()) {
                request.put("system", systemPrompt);
            }

            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", userPrompt);
            messages.add(userMessage);
            request.set("messages", messages);

            String response = webClient.post()
                    .bodyValue(request.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(120))
                    .block();

            return extractTextFromResponse(response);
        } catch (Exception e) {
            log.error("CoreAIService error calling Claude API: {}", e.getMessage(), e);
            throw new RuntimeException("Claude API call failed: " + e.getMessage(), e);
        }
    }

    private String extractTextFromResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode content = root.path("content");
            if (content.isArray() && !content.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode block : content) {
                    if ("text".equals(block.path("type").asText())) {
                        sb.append(block.path("text").asText());
                    }
                }
                return sb.toString();
            }
            return response;
        } catch (Exception e) {
            log.error("Failed to parse Claude response", e);
            return response;
        }
    }
}
