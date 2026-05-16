package com.contentiq.contentiq.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class HuggingFaceSentimentClient {

    public record SentimentResult(String label, double score) { }

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String model;
    private final int batchSize;

    public HuggingFaceSentimentClient(
            @Value("${huggingface.api.key}") String apiKey,
            @Value("${huggingface.api.url:https://api-inference.huggingface.co/models}") String baseUrl,
            @Value("${huggingface.sentiment.model:cardiffnlp/twitter-roberta-base-sentiment-latest}") String model,
            @Value("${huggingface.batch-size:20}") int batchSize) {
        this.model = model;
        this.batchSize = Math.max(1, batchSize);
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public List<SentimentResult> classify(List<String> texts) {
        List<SentimentResult> all = new ArrayList<>(texts.size());
        for (int i = 0; i < texts.size(); i += batchSize) {
            List<String> chunk = texts.subList(i, Math.min(i + batchSize, texts.size()));
            all.addAll(classifyBatch(chunk));
        }
        return all;
    }

    private List<SentimentResult> classifyBatch(List<String> texts) {
        ObjectNode body = objectMapper.createObjectNode();
        ArrayNode inputs = body.putArray("inputs");
        for (String t : texts) {
            inputs.add(t == null ? "" : truncate(t, 500));
        }
        ObjectNode options = body.putObject("options");
        options.put("wait_for_model", true);

        try {
            String response = webClient.post()
                    .uri("/" + model)
                    .bodyValue(body.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(120))
                    .block();
            return parse(response, texts.size());
        } catch (Exception e) {
            log.error("HuggingFace API error: {}", e.getMessage(), e);
            return neutralFallback(texts.size());
        }
    }

    private List<SentimentResult> parse(String response, int expected) {
        List<SentimentResult> out = new ArrayList<>(expected);
        try {
            JsonNode root = objectMapper.readTree(response);
            if (root.isObject() && root.has("error")) {
                log.warn("HuggingFace error response: {}", root.path("error").asText());
                return neutralFallback(expected);
            }
            if (root.isArray()) {
                if (root.size() == 1 && root.get(0).isArray() && root.get(0).size() == expected) {
                    for (JsonNode entry : root.get(0)) {
                        out.add(new SentimentResult(
                                normalize(entry.path("label").asText("neutral")),
                                entry.path("score").asDouble(0.0)));
                    }
                } else {
                    for (JsonNode item : root) {
                        if (item.isArray()) {
                            out.add(topLabel(item));
                        } else if (item.isObject() && item.has("label")) {
                            out.add(new SentimentResult(
                                    normalize(item.get("label").asText()),
                                    item.path("score").asDouble(0.0)));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse HuggingFace response: {}", e.getMessage());
        }
        while (out.size() < expected) {
            out.add(new SentimentResult("NEUTRAL", 0.0));
        }
        return out;
    }

    private SentimentResult topLabel(JsonNode scoreArray) {
        String bestLabel = "NEUTRAL";
        double bestScore = -1.0;
        for (JsonNode candidate : scoreArray) {
            double s = candidate.path("score").asDouble(0.0);
            if (s > bestScore) {
                bestScore = s;
                bestLabel = normalize(candidate.path("label").asText("neutral"));
            }
        }
        return new SentimentResult(bestLabel, Math.max(0.0, bestScore));
    }

    private String normalize(String label) {
        if (label == null) return "NEUTRAL";
        return switch (label.toLowerCase()) {
            case "positive", "label_2" -> "POSITIVE";
            case "negative", "label_0" -> "NEGATIVE";
            default -> "NEUTRAL";
        };
    }

    private List<SentimentResult> neutralFallback(int size) {
        List<SentimentResult> out = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            out.add(new SentimentResult("NEUTRAL", 0.0));
        }
        return out;
    }

    private String truncate(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max);
    }
}
