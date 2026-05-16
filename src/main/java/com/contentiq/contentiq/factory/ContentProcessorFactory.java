package com.contentiq.contentiq.factory;

import com.contentiq.contentiq.strategy.AnalysisStrategy;
import com.contentiq.contentiq.strategy.CommentSummaryStrategy;
import com.contentiq.contentiq.strategy.NotesGenerationStrategy;
import com.contentiq.contentiq.strategy.SentimentAnalysisStrategy;
import org.springframework.stereotype.Component;

@Component
public class ContentProcessorFactory {

    public enum ProcessorType {
        SENTIMENT,
        COMMENT_SUMMARY,
        NOTES
    }

    private final SentimentAnalysisStrategy sentimentStrategy;
    private final CommentSummaryStrategy commentSummaryStrategy;
    private final NotesGenerationStrategy notesGenerationStrategy;

    public ContentProcessorFactory(SentimentAnalysisStrategy sentimentStrategy,
                                   CommentSummaryStrategy commentSummaryStrategy,
                                   NotesGenerationStrategy notesGenerationStrategy) {
        this.sentimentStrategy = sentimentStrategy;
        this.commentSummaryStrategy = commentSummaryStrategy;
        this.notesGenerationStrategy = notesGenerationStrategy;
    }

    public AnalysisStrategy<?, ?> getStrategy(ProcessorType type) {
        return switch (type) {
            case SENTIMENT -> sentimentStrategy;
            case COMMENT_SUMMARY -> commentSummaryStrategy;
            case NOTES -> notesGenerationStrategy;
        };
    }

    public SentimentAnalysisStrategy sentiment() {
        return sentimentStrategy;
    }

    public CommentSummaryStrategy commentSummary() {
        return commentSummaryStrategy;
    }

    public NotesGenerationStrategy notes() {
        return notesGenerationStrategy;
    }
}
