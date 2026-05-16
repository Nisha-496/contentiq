package com.contentiq.contentiq.service;

import com.contentiq.contentiq.exception.ResourceNotFoundException;
import com.contentiq.contentiq.model.Comment;
import com.contentiq.contentiq.model.Video;
import com.contentiq.contentiq.util.VideoUrlParser;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CommentSnippet;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.api.services.youtube.model.VideoListResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class YouTubeService {

    private static final List<String> VIDEO_PARTS = List.of("snippet", "contentDetails", "statistics");
    private static final List<String> COMMENT_PARTS = List.of("snippet");

    private final YouTube youTube;
    private final String apiKey;
    private final long maxPerPage;
    private final long maxTotal;

    public YouTubeService(YouTube youTube,
                          @Value("${youtube.api.key}") String apiKey,
                          @Value("${youtube.comments.max-per-page:100}") long maxPerPage,
                          @Value("${youtube.comments.max-total:500}") long maxTotal) {
        this.youTube = youTube;
        this.apiKey = apiKey;
        this.maxPerPage = Math.min(100, Math.max(1, maxPerPage));
        this.maxTotal = Math.max(1, maxTotal);
    }

    public Video fetchVideoMetadata(String urlOrId) {
        String videoId = VideoUrlParser.extractVideoId(urlOrId);
        try {
            VideoListResponse response = youTube.videos()
                    .list(VIDEO_PARTS)
                    .setId(List.of(videoId))
                    .setKey(apiKey)
                    .execute();

            if (response.getItems() == null || response.getItems().isEmpty()) {
                throw new ResourceNotFoundException("YouTube video not found: " + videoId);
            }

            com.google.api.services.youtube.model.Video yt = response.getItems().get(0);
            var snippet = yt.getSnippet();
            var details = yt.getContentDetails();

            return Video.builder()
                    .title(snippet == null ? null : snippet.getTitle())
                    .url("https://www.youtube.com/watch?v=" + videoId)
                    .channelName(snippet == null ? null : snippet.getChannelTitle())
                    .description(snippet == null ? null : snippet.getDescription())
                    .durationSeconds(details == null ? null : parseIsoDurationSeconds(details.getDuration()))
                    .build();
        } catch (IOException e) {
            log.error("YouTube API error fetching video {}: {}", videoId, e.getMessage(), e);
            throw new RuntimeException("YouTube API error: " + e.getMessage(), e);
        }
    }

    public List<Comment> fetchComments(String urlOrId, String storedVideoId, long maxResults) {
        String youTubeVideoId = VideoUrlParser.extractVideoId(urlOrId);
        long cap = maxResults > 0 ? Math.min(maxResults, maxTotal) : maxTotal;

        List<Comment> result = new ArrayList<>();
        String pageToken = null;

        try {
            while (result.size() < cap) {
                long remaining = cap - result.size();
                long pageSize = Math.min(maxPerPage, remaining);

                var request = youTube.commentThreads()
                        .list(COMMENT_PARTS)
                        .setVideoId(youTubeVideoId)
                        .setMaxResults(pageSize)
                        .setKey(apiKey)
                        .setTextFormat("plainText");

                if (pageToken != null) {
                    request.setPageToken(pageToken);
                }

                CommentThreadListResponse response = request.execute();
                List<CommentThread> threads = response.getItems();
                if (threads == null || threads.isEmpty()) {
                    break;
                }

                for (CommentThread thread : threads) {
                    if (result.size() >= cap) break;
                    CommentSnippet top = thread.getSnippet() == null
                            || thread.getSnippet().getTopLevelComment() == null
                            ? null
                            : thread.getSnippet().getTopLevelComment().getSnippet();
                    if (top == null) continue;

                    result.add(Comment.builder()
                            .videoId(storedVideoId)
                            .author(top.getAuthorDisplayName())
                            .text(top.getTextDisplay())
                            .likeCount(top.getLikeCount() == null ? 0 : top.getLikeCount().intValue())
                            .publishedAt(toLocalDateTime(top.getPublishedAt() == null ? null
                                    : top.getPublishedAt().toStringRfc3339()))
                            .analyzed(false)
                            .build());
                }

                pageToken = response.getNextPageToken();
                if (pageToken == null) {
                    break;
                }
            }
            log.info("Fetched {} comments for YouTube video {}", result.size(), youTubeVideoId);
            return result;
        } catch (IOException e) {
            log.error("YouTube API error fetching comments for {}: {}", youTubeVideoId, e.getMessage(), e);
            throw new RuntimeException("YouTube API error: " + e.getMessage(), e);
        }
    }

    private LocalDateTime toLocalDateTime(String rfc3339) {
        if (rfc3339 == null || rfc3339.isBlank()) return null;
        try {
            return LocalDateTime.ofInstant(Instant.parse(rfc3339), ZoneId.systemDefault());
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseIsoDurationSeconds(String iso) {
        if (iso == null || iso.isBlank()) return null;
        try {
            return java.time.Duration.parse(iso).toSeconds();
        } catch (Exception e) {
            return null;
        }
    }
}
