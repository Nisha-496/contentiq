package com.contentiq.contentiq.strategy;

import com.contentiq.contentiq.model.Comment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class CommentSummaryStrategy implements AnalysisStrategy<List<Comment>, String> {

    private static final String NAME = "COMMENT_SUMMARY";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String analyze(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return "No comments available for summary.";
        }

        int total = comments.size();
        int positive = 0, negative = 0, neutral = 0;
        long totalLikes = 0;
        Comment topPositive = null;
        Comment topNegative = null;
        Comment topOverall = null;

        for (Comment c : comments) {
            String s = c.getSentiment() == null ? "NEUTRAL" : c.getSentiment().toUpperCase();
            int likes = c.getLikeCount() == null ? 0 : c.getLikeCount();
            totalLikes += likes;

            switch (s) {
                case "POSITIVE" -> {
                    positive++;
                    if (likes > likeCount(topPositive)) topPositive = c;
                }
                case "NEGATIVE" -> {
                    negative++;
                    if (likes > likeCount(topNegative)) topNegative = c;
                }
                default -> neutral++;
            }
            if (likes > likeCount(topOverall)) topOverall = c;
        }

        String dominant = dominantLabel(positive, negative, neutral);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Analyzed %d comments with %d total likes. ", total, totalLikes));
        sb.append(String.format(
                "Sentiment breakdown: %d positive (%.1f%%), %d neutral (%.1f%%), %d negative (%.1f%%). ",
                positive, pct(positive, total),
                neutral, pct(neutral, total),
                negative, pct(negative, total)));
        sb.append("Overall audience reaction is dominated by ").append(dominant).append(" feedback.");

        if (topOverall != null) {
            sb.append(" Most-liked comment overall by ").append(safeAuthor(topOverall))
                    .append(" (").append(likeCount(topOverall)).append(" likes): ")
                    .append(snippet(topOverall.getText())).append('.');
        }
        if (topPositive != null && topPositive != topOverall) {
            sb.append(" Top positive comment by ").append(safeAuthor(topPositive))
                    .append(" (").append(likeCount(topPositive)).append(" likes): ")
                    .append(snippet(topPositive.getText())).append('.');
        }
        if (topNegative != null && topNegative != topOverall) {
            sb.append(" Top negative comment by ").append(safeAuthor(topNegative))
                    .append(" (").append(likeCount(topNegative)).append(" likes): ")
                    .append(snippet(topNegative.getText())).append('.');
        }
        return sb.toString();
    }

    private String dominantLabel(int pos, int neg, int neu) {
        if (pos >= neg && pos >= neu) return "POSITIVE";
        if (neg >= neu) return "NEGATIVE";
        return "NEUTRAL";
    }

    private double pct(int part, int total) {
        return total == 0 ? 0.0 : 100.0 * part / total;
    }

    private int likeCount(Comment c) {
        if (c == null || c.getLikeCount() == null) return -1;
        return c.getLikeCount();
    }

    private String safeAuthor(Comment c) {
        return c.getAuthor() == null || c.getAuthor().isBlank() ? "(anonymous)" : c.getAuthor();
    }

    private String snippet(String text) {
        if (text == null) return "\"\"";
        String t = text.replace("\n", " ").trim();
        return t.length() <= 120 ? "\"" + t + "\"" : "\"" + t.substring(0, 120) + "...\"";
    }
}
