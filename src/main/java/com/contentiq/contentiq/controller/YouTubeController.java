package com.contentiq.contentiq.controller;

import com.contentiq.contentiq.dto.YouTubeImportRequest;
import com.contentiq.contentiq.model.Comment;
import com.contentiq.contentiq.model.User;
import com.contentiq.contentiq.model.Video;
import com.contentiq.contentiq.repository.CommentRepository;
import com.contentiq.contentiq.repository.VideoRepository;
import com.contentiq.contentiq.service.UserService;
import com.contentiq.contentiq.service.YouTubeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/youtube")
public class YouTubeController {

    private final YouTubeService youTubeService;
    private final VideoRepository videoRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;

    public YouTubeController(YouTubeService youTubeService,
                             VideoRepository videoRepository,
                             CommentRepository commentRepository,
                             UserService userService) {
        this.youTubeService = youTubeService;
        this.videoRepository = videoRepository;
        this.commentRepository = commentRepository;
        this.userService = userService;
    }

    @GetMapping("/metadata")
    public ResponseEntity<?> preview(@RequestParam("urlOrId") String urlOrId) {
        return ResponseEntity.ok(youTubeService.fetchVideoMetadata(urlOrId));
    }

    @PostMapping("/videos/import")
    public ResponseEntity<?> importVideo(@Valid @RequestBody YouTubeImportRequest request,
                                         @AuthenticationPrincipal UserDetails principal) {
        User user = userService.findByUsername(principal.getUsername());
        Video metadata = youTubeService.fetchVideoMetadata(request.getUrlOrId());
        metadata.setTranscript(request.getTranscript());
        metadata.setOwnerUserId(user.getId());
        Video saved = videoRepository.save(metadata);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/videos/{videoId}/comments/import")
    public ResponseEntity<?> importComments(@PathVariable String videoId,
                                            @RequestParam(value = "maxResults", defaultValue = "0") long maxResults) {
        Video video = videoRepository.findById(videoId).orElse(null);
        if (video == null) {
            Map<String, Object> body = new HashMap<>();
            body.put("error", "Video not found: " + videoId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }
        List<Comment> fetched = youTubeService.fetchComments(video.getUrl(), videoId, maxResults);
        List<Comment> saved = commentRepository.saveAll(fetched);

        Map<String, Object> response = new HashMap<>();
        response.put("videoId", videoId);
        response.put("imported", saved.size());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
