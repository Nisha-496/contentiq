package com.contentiq.contentiq.strategy;

import com.contentiq.contentiq.decorator.AIService;
import com.contentiq.contentiq.model.NotesDocument;
import com.contentiq.contentiq.model.Video;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class NotesGenerationStrategy implements AnalysisStrategy<Video, NotesDocument> {

    private static final String NAME = "NOTES";

    private static final String SYSTEM_PROMPT = """
            You produce structured study notes from a YouTube video transcript.
            Output strict JSON in this exact form:
            {
              "title": "...",
              "overview": "1-3 sentence overview",
              "sections": [
                { "heading": "...", "content": "...", "order": 1 }
              ],
              "keyTakeaways": ["..."],
              "actionItems": ["..."]
            }
            Use 4-8 sections. Keep each section content under 200 words.
            Return ONLY JSON, no markdown fences, no prose.
            """;

    private final AIService aiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NotesGenerationStrategy(AIService aiService) {
        this.aiService = aiService;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public NotesDocument analyze(Video video) {
        if (video == null || video.getTranscript() == null || video.getTranscript().isBlank()) {
            throw new IllegalArgumentException("Video transcript is required for notes generation");
        }

        String userPrompt = "Video title: " + (video.getTitle() == null ? "Untitled" : video.getTitle())
                + "\n\nTranscript:\n" + video.getTranscript();

        String raw = aiService.generate(SYSTEM_PROMPT, userPrompt);
        return parseNotes(raw, video);
    }

    private NotesDocument parseNotes(String raw, Video video) {
        try {
            String json = extractJson(raw);
            JsonNode root = objectMapper.readTree(json);

            List<NotesDocument.Section> sections = new ArrayList<>();
            JsonNode sectionsNode = root.path("sections");
            if (sectionsNode.isArray()) {
                int order = 1;
                for (JsonNode s : sectionsNode) {
                    sections.add(NotesDocument.Section.builder()
                            .heading(s.path("heading").asText("Untitled"))
                            .content(s.path("content").asText(""))
                            .order(s.path("order").asInt(order++))
                            .build());
                }
            }

            List<String> takeaways = new ArrayList<>();
            for (JsonNode n : root.path("keyTakeaways")) {
                takeaways.add(n.asText());
            }

            List<String> actions = new ArrayList<>();
            for (JsonNode n : root.path("actionItems")) {
                actions.add(n.asText());
            }

            return NotesDocument.builder()
                    .videoId(video.getId())
                    .title(root.path("title").asText(video.getTitle()))
                    .overview(root.path("overview").asText(""))
                    .sections(sections)
                    .keyTakeaways(takeaways)
                    .actionItems(actions)
                    .status("COMPLETED")
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse notes JSON: {}", e.getMessage());
            return NotesDocument.builder()
                    .videoId(video.getId())
                    .title(video.getTitle())
                    .overview("Failed to parse AI response.")
                    .status("FAILED")
                    .build();
        }
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
}
