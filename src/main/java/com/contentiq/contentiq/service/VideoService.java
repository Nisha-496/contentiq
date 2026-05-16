package com.contentiq.contentiq.service;

import com.contentiq.contentiq.dto.VideoRequest;
import com.contentiq.contentiq.exception.ResourceNotFoundException;
import com.contentiq.contentiq.model.Video;
import com.contentiq.contentiq.repository.VideoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class VideoService {

    private final VideoRepository videoRepository;

    public VideoService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    public Video create(VideoRequest request, String ownerUserId) {
        Video video = Video.builder()
                .title(request.getTitle())
                .url(request.getUrl())
                .channelName(request.getChannelName())
                .description(request.getDescription())
                .transcript(request.getTranscript())
                .durationSeconds(request.getDurationSeconds())
                .ownerUserId(ownerUserId)
                .build();
        Video saved = videoRepository.save(video);
        log.info("Created video {} owner={}", saved.getId(), ownerUserId);
        return saved;
    }

    public Video getById(String id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found: " + id));
    }

    public List<Video> getAll() {
        return videoRepository.findAll();
    }

    public List<Video> getByOwner(String ownerUserId) {
        return videoRepository.findByOwnerUserId(ownerUserId);
    }

    public Video update(String id, VideoRequest request) {
        Video existing = getById(id);
        existing.setTitle(request.getTitle());
        existing.setUrl(request.getUrl());
        existing.setChannelName(request.getChannelName());
        existing.setDescription(request.getDescription());
        existing.setTranscript(request.getTranscript());
        existing.setDurationSeconds(request.getDurationSeconds());
        existing.setUpdatedAt(LocalDateTime.now());
        return videoRepository.save(existing);
    }

    public void delete(String id) {
        if (!videoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Video not found: " + id);
        }
        videoRepository.deleteById(id);
    }
}
