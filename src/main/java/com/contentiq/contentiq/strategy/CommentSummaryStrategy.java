package com.contentiq.contentiq.strategy;

import com.contentiq.contentiq.decorator.AIService;
import com.contentiq.contentiq.model.Comment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class CommentSummaryStrategy implements AnalysisStrategy<List<Comment>, String> {

    private static final String NAME = "COMMENT_SUMMARY";

    private static final String SYSTEM_PROMPT = """
            You are an analyst summarizing YouTube audience feedback.
            Produce a concise summary (max 250 words) covering:
              - Overall sentiment trend
              - Most common questions
              - Most common complaints
              - Notable praise
              - Any spam or off-topic patterns
            Respond in plain prose, no markdown headers.
            """;

    private final AIService aiService;

    public CommentSummaryStrategy(AIService aiService) {
        this.aiService = aiService;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String analyze(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return "No comments available for summary.";
        }

        StringBuilder sb = new StringBuilder("Summarize these comments:\n");
        for (Comment c : comments) {
            sb.append("- [").append(c.getSentiment() == null ? "?" : c.getSentiment())
                    .append("/").append(c.getCategory() == null ? "?" : c.getCategory())
                    .append("] ").append(safe(c.getText())).append('\n');
        }
        return aiService.generate(SYSTEM_PROMPT, sb.toString());
    }

    private String safe(String text) {
        if (text == null) return "";
        return text.replace("\n", " ");
    }
}
