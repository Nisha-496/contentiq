package com.contentiq.contentiq.strategy;

import com.contentiq.contentiq.model.Comment;
import com.contentiq.contentiq.service.HuggingFaceSentimentClient;
import com.contentiq.contentiq.service.HuggingFaceSentimentClient.SentimentResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SentimentAnalysisStrategy implements AnalysisStrategy<List<Comment>, List<Comment>> {

    private static final String NAME = "SENTIMENT";
    private static final String DEFAULT_CATEGORY = "GENERAL";

    private final HuggingFaceSentimentClient client;

    public SentimentAnalysisStrategy(HuggingFaceSentimentClient client) {
        this.client = client;
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

        List<String> texts = comments.stream()
                .map(c -> c.getText() == null ? "" : c.getText())
                .toList();

        List<SentimentResult> results = client.classify(texts);
        log.info("HuggingFace classified {} comments", results.size());

        for (int i = 0; i < comments.size(); i++) {
            Comment c = comments.get(i);
            SentimentResult r = i < results.size() ? results.get(i) : new SentimentResult("NEUTRAL", 0.0);
            c.setSentiment(r.label());
            c.setConfidence(r.score());
            if (c.getCategory() == null || c.getCategory().isBlank()) {
                c.setCategory(DEFAULT_CATEGORY);
            }
            c.setAnalyzed(true);
        }
        return comments;
    }
}
