package com.contentiq.contentiq.strategy;

import com.contentiq.contentiq.model.Comment;
import com.contentiq.contentiq.service.HuggingFaceSentimentClient;
import com.contentiq.contentiq.service.HuggingFaceSentimentClient.SentimentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SentimentAnalysisStrategyTest {

    private HuggingFaceSentimentClient client;
    private SentimentAnalysisStrategy strategy;

    @BeforeEach
    void setUp() {
        client = mock(HuggingFaceSentimentClient.class);
        strategy = new SentimentAnalysisStrategy(client);
    }

    @Test
    void emptyInputReturnsEmpty() {
        assertThat(strategy.analyze(List.of())).isEmpty();
        assertThat(strategy.analyze(null)).isEmpty();
    }

    @Test
    void appliesSentimentResultsToCommentsInOrder() {
        List<Comment> input = List.of(
                Comment.builder().text("great!").build(),
                Comment.builder().text("bad").build(),
                Comment.builder().text("meh").build()
        );
        when(client.classify(anyList())).thenReturn(List.of(
                new SentimentResult("POSITIVE", 0.95),
                new SentimentResult("NEGATIVE", 0.88),
                new SentimentResult("NEUTRAL", 0.55)
        ));

        List<Comment> result = strategy.analyze(input);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getSentiment()).isEqualTo("POSITIVE");
        assertThat(result.get(0).getConfidence()).isEqualTo(0.95);
        assertThat(result.get(1).getSentiment()).isEqualTo("NEGATIVE");
        assertThat(result.get(2).getSentiment()).isEqualTo("NEUTRAL");
    }

    @Test
    void marksAllCommentsAsAnalyzed() {
        List<Comment> input = List.of(
                Comment.builder().text("a").build(),
                Comment.builder().text("b").build()
        );
        when(client.classify(anyList())).thenReturn(List.of(
                new SentimentResult("POSITIVE", 0.9),
                new SentimentResult("NEGATIVE", 0.8)
        ));

        List<Comment> result = strategy.analyze(input);

        assertThat(result).allMatch(c -> Boolean.TRUE.equals(c.getAnalyzed()));
    }

    @Test
    void defaultsCategoryToGeneralWhenMissing() {
        Comment c = Comment.builder().text("hello").build();
        when(client.classify(anyList())).thenReturn(List.of(
                new SentimentResult("POSITIVE", 0.9)
        ));

        List<Comment> result = strategy.analyze(List.of(c));

        assertThat(result.get(0).getCategory()).isEqualTo("GENERAL");
    }

    @Test
    void preservesExistingCategoryWhenSet() {
        Comment c = Comment.builder().text("hello").category("QUESTION").build();
        when(client.classify(anyList())).thenReturn(List.of(
                new SentimentResult("POSITIVE", 0.9)
        ));

        List<Comment> result = strategy.analyze(List.of(c));

        assertThat(result.get(0).getCategory()).isEqualTo("QUESTION");
    }

    @Test
    void fallsBackToNeutralWhenClientReturnsFewer() {
        List<Comment> input = List.of(
                Comment.builder().text("a").build(),
                Comment.builder().text("b").build()
        );
        when(client.classify(anyList())).thenReturn(List.of(
                new SentimentResult("POSITIVE", 0.9)
        ));

        List<Comment> result = strategy.analyze(input);

        assertThat(result.get(0).getSentiment()).isEqualTo("POSITIVE");
        assertThat(result.get(1).getSentiment()).isEqualTo("NEUTRAL");
        assertThat(result.get(1).getConfidence()).isEqualTo(0.0);
    }

    @Test
    void passesCommentTextsToClient() {
        List<Comment> input = List.of(
                Comment.builder().text("first text").build(),
                Comment.builder().text("second text").build()
        );
        when(client.classify(anyList())).thenReturn(List.of(
                new SentimentResult("POSITIVE", 0.9),
                new SentimentResult("NEGATIVE", 0.8)
        ));

        strategy.analyze(input);

        verify(client).classify(List.of("first text", "second text"));
    }

    @Test
    void handlesNullCommentTextAsEmpty() {
        List<Comment> input = List.of(Comment.builder().text(null).build());
        when(client.classify(anyList())).thenReturn(List.of(
                new SentimentResult("NEUTRAL", 0.5)
        ));

        strategy.analyze(input);

        verify(client).classify(List.of(""));
    }

    @Test
    void strategyNameIsSentiment() {
        assertThat(strategy.getName()).isEqualTo("SENTIMENT");
    }
}
