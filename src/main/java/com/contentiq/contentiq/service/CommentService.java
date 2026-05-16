package com.contentiq.contentiq.service;

import com.contentiq.contentiq.dto.BulkCommentRequest;
import com.contentiq.contentiq.dto.CommentRequest;
import com.contentiq.contentiq.exception.ResourceNotFoundException;
import com.contentiq.contentiq.model.Comment;
import com.contentiq.contentiq.repository.CommentRepository;
import com.contentiq.contentiq.repository.VideoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final VideoRepository videoRepository;

    public CommentService(CommentRepository commentRepository, VideoRepository videoRepository) {
        this.commentRepository = commentRepository;
        this.videoRepository = videoRepository;
    }

    public List<Comment> saveBulk(BulkCommentRequest request) {
        if (!videoRepository.existsById(request.getVideoId())) {
            throw new ResourceNotFoundException("Video not found: " + request.getVideoId());
        }
        List<Comment> comments = new ArrayList<>(request.getComments().size());
        for (CommentRequest cr : request.getComments()) {
            comments.add(Comment.builder()
                    .videoId(request.getVideoId())
                    .author(cr.getAuthor())
                    .text(cr.getText())
                    .likeCount(cr.getLikeCount() == null ? 0 : cr.getLikeCount())
                    .publishedAt(cr.getPublishedAt())
                    .analyzed(false)
                    .build());
        }
        List<Comment> saved = commentRepository.saveAll(comments);
        log.info("Saved {} comments for video {}", saved.size(), request.getVideoId());
        return saved;
    }

    public List<Comment> getByVideoId(String videoId) {
        if (!videoRepository.existsById(videoId)) {
            throw new ResourceNotFoundException("Video not found: " + videoId);
        }
        return commentRepository.findByVideoId(videoId);
    }

    public List<Comment> getUnanalyzedByVideoId(String videoId) {
        return commentRepository.findByVideoIdAndAnalyzed(videoId, false);
    }

    public void saveAll(List<Comment> comments) {
        commentRepository.saveAll(comments);
    }
}
