package com.contentiq.contentiq.strategy;

import com.contentiq.contentiq.model.Comment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommentSummaryStrategyTest {

    private final CommentSummaryStrategy strategy = new CommentSummaryStrategy();

    @Test
    void emptyListReturnsFallbackMessage() {
        assertThat(strategy.analyze(List.of()))
                .isEqualTo("No comments available for summary.");
        assertThat(strategy.analyze(null))
                .isEqualTo("No comments available for summary.");
    }

    @Test
    void countsAndPercentagesAreCorrect() {
        List<Comment> comments = List.of(
                buildComment("alice", "great!", "POSITIVE", 5),
                buildComment("bob", "loved it", "POSITIVE", 3),
                buildComment("carol", "boring", "NEGATIVE", 1),
                buildComment("dave", "meh", "NEUTRAL", 0)
        );

        String summary = strategy.analyze(comments);

        assertThat(summary).contains("Analyzed 4 comments");
        assertThat(summary).contains("2 positive (50.0%)");
        assertThat(summary).contains("1 neutral (25.0%)");
        assertThat(summary).contains("1 negative (25.0%)");
        assertThat(summary).contains("9 total likes");
    }

    @Test
    void identifiesPositiveAsDominantWhenMajority() {
        List<Comment> comments = List.of(
                buildComment("a", "g", "POSITIVE", 0),
                buildComment("b", "g", "POSITIVE", 0),
                buildComment("c", "b", "NEGATIVE", 0)
        );
        assertThat(strategy.analyze(comments))
                .contains("dominated by POSITIVE feedback");
    }

    @Test
    void identifiesNegativeAsDominant() {
        List<Comment> comments = List.of(
                buildComment("a", "b", "NEGATIVE", 0),
                buildComment("b", "b", "NEGATIVE", 0),
                buildComment("c", "n", "NEUTRAL", 0)
        );
        assertThat(strategy.analyze(comments))
                .contains("dominated by NEGATIVE feedback");
    }

    @Test
    void identifiesNeutralAsDominant() {
        List<Comment> comments = List.of(
                buildComment("a", "n", "NEUTRAL", 0),
                buildComment("b", "n", "NEUTRAL", 0),
                buildComment("c", "n", "NEUTRAL", 0)
        );
        assertThat(strategy.analyze(comments))
                .contains("dominated by NEUTRAL feedback");
    }

    @Test
    void mostLikedCommentIsHighlighted() {
        List<Comment> comments = List.of(
                buildComment("@viral", "this took off", "POSITIVE", 1_000_000),
                buildComment("@random", "ok", "NEUTRAL", 2)
        );
        String summary = strategy.analyze(comments);
        assertThat(summary).contains("@viral");
        assertThat(summary).contains("1000000");
    }

    @Test
    void anonymousAuthorIsShownAsPlaceholder() {
        Comment c = buildComment(null, "no author", "POSITIVE", 5);
        String summary = strategy.analyze(List.of(c));
        assertThat(summary).contains("(anonymous)");
    }

    @Test
    void longCommentsAreTruncatedInSnippet() {
        String longText = "a".repeat(200);
        Comment c = buildComment("@alice", longText, "POSITIVE", 1);
        String summary = strategy.analyze(List.of(c));
        assertThat(summary).contains("...");
    }

    @Test
    void missingSentimentTreatedAsNeutral() {
        Comment c = Comment.builder()
                .author("@x")
                .text("hi")
                .likeCount(0)
                .build();
        String summary = strategy.analyze(List.of(c));
        assertThat(summary).contains("1 neutral");
    }

    private Comment buildComment(String author, String text, String sentiment, int likes) {
        return Comment.builder()
                .author(author)
                .text(text)
                .sentiment(sentiment)
                .likeCount(likes)
                .build();
    }
}
