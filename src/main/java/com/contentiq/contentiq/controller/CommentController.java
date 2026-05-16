package com.contentiq.contentiq.controller;

import com.contentiq.contentiq.dto.BulkCommentRequest;
import com.contentiq.contentiq.model.Comment;
import com.contentiq.contentiq.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> createBulk(@Valid @RequestBody BulkCommentRequest request) {
        List<Comment> saved = commentService.saveBulk(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{videoId}")
    public ResponseEntity<?> getByVideo(@PathVariable String videoId) {
        return ResponseEntity.ok(commentService.getByVideoId(videoId));
    }
}
