package com.contentiq.contentiq.strategy;

import com.contentiq.contentiq.decorator.AIService;
import com.contentiq.contentiq.model.Comment;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SentimentAnalysisStrategy implements AnalysisStrategy<List<Comment>, List<Comment>> {

    private static final String NAME = "SENTIMENT";

    private static final String SYSTEM_PROMPT = """
            You are a YouTube comment classification engine.
            For each comment classify:
              sentiment: POSITIVE | NEGATIVE | NEUTRAL
              category : QUESTION | COMPLAINT | SPAM | GENERAL
              confidence: 0.0 - 1.0
            Return ONLY valid JSON, no prose, in this exact form:
            { "results": [ { "id": "<id>", "sentiment": "...", "category": "...", "confidence": 0.xx } ] }
            """;

    private final AIService aiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SentimentAnalysisStrategy(AIService aiService) {
        this.aiService = aiService;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<Comment> analyze(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return List.of();
        }

        StringBuilder userPrompt = new StringBuilder("Classify these comments:\n");
        for (Comment c : comments) {
            userPrompt.append("- id=").append(c.getId())
                    .append(" text=").append(safe(c.getText()))
                    .append('\n');
        }

        String raw = aiService.generate(SYSTEM_PROMPT, userPrompt.toString());
        Map<String, JsonNode> byId = parseResults(raw);

        List<Comment> updated = new ArrayList<>(comments.size());
        for (Comment c : comments) {
            JsonNode node = byId.get(c.getId());
            if (node != null) {
                c.setSentiment(text(node, "sentiment", "NEUTRAL"));
                c.setCategory(text(node, "category", "GENERAL"));
                c.setConfidence(node.path("confidence").asDouble(0.5));
            } else {
                c.setSentiment("NEUTRAL");
                c.setCategory("GENERAL");
                c.setConfidence(0.0);
            }
            c.setAnalyzed(true);
            updated.add(c);
        }
        return updated;
    }

    private Map<String, JsonNode> parseResults(String raw) {
        Map<String, JsonNode> map = new HashMap<>();
        try {
            String json = extractJson(raw);
            JsonNode root = objectMapper.readTree(json);
            JsonNode results = root.path("results");
            if (results.isArray()) {
                for (JsonNode node : results) {
                    String id = node.path("id").asText(null);
                    if (id != null) {
                        map.put(id, node);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse sentiment results: {}", e.getMessage());
        }
        return map;
    }

    private String extractJson(String raw) {
        if (raw == null) return "{}";
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return raw.substring(start, end + 1);
        }
        return raw;
    }

    private String text(JsonNode node, String field, String fallback) {
        String v = node.path(field).asText(fallback);
        return v == null || v.isBlank() ? fallback : v.toUpperCase();
    }

    private String safe(String text) {
        if (text == null) return "";
        return text.replace("\n", " ").replace("\"", "'");
    }
}
