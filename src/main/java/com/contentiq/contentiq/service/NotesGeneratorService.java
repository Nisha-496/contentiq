package com.contentiq.contentiq.service;

import com.contentiq.contentiq.factory.ContentProcessorFactory;
import com.contentiq.contentiq.model.AnalysisReport;
import com.contentiq.contentiq.model.NotesDocument;
import com.contentiq.contentiq.model.Video;
import com.contentiq.contentiq.observer.AnalysisEventPublisher;
import com.contentiq.contentiq.repository.AnalysisReportRepository;
import com.contentiq.contentiq.repository.NotesDocumentRepository;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class NotesGeneratorService {

    protected final VideoService videoService;
    protected final ContentProcessorFactory factory;
    protected final NotesDocumentRepository notesRepository;
    protected final AnalysisReportRepository reportRepository;
    protected final AnalysisEventPublisher eventPublisher;

    protected NotesGeneratorService(VideoService videoService,
                                    ContentProcessorFactory factory,
                                    NotesDocumentRepository notesRepository,
                                    AnalysisReportRepository reportRepository,
                                    AnalysisEventPublisher eventPublisher) {
        this.videoService = videoService;
        this.factory = factory;
        this.notesRepository = notesRepository;
        this.reportRepository = reportRepository;
        this.eventPublisher = eventPublisher;
    }

    public final AnalysisReport generate(String videoId) {
        AnalysisReport report = AnalysisReport.builder()
                .videoId(videoId)
                .reportType("NOTES")
                .status("IN_PROGRESS")
                .build();
        report = reportRepository.save(report);

        try {
            Video video = fetchVideo(videoId);
            validate(video);
            NotesDocument notes = produce(video);
            NotesDocument savedNotes = persist(notes);
            return finalize(report, savedNotes);
        } catch (Exception e) {
            log.error("Notes generation failed for video {}: {}", videoId, e.getMessage(), e);
            report.setStatus("FAILED");
            report.setErrorMessage(e.getMessage());
            report.setCompletedAt(LocalDateTime.now());
            return reportRepository.save(report);
        }
    }

    protected Video fetchVideo(String videoId) {
        return videoService.getById(videoId);
    }

    protected void validate(Video video) {
        if (video.getTranscript() == null || video.getTranscript().isBlank()) {
            throw new IllegalArgumentException("Video has no transcript to generate notes from");
        }
    }

    protected abstract NotesDocument produce(Video video);

    protected NotesDocument persist(NotesDocument notes) {
        return notesRepository.save(notes);
    }

    protected AnalysisReport finalize(AnalysisReport report, NotesDocument notes) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("notesId", notes.getId());
        metadata.put("sectionCount", notes.getSections() == null ? 0 : notes.getSections().size());

        report.setStatus("COMPLETED");
        report.setSummary(notes.getOverview());
        report.setMetadata(metadata);
        report.setTotalItems(notes.getSections() == null ? 0 : notes.getSections().size());
        report.setCompletedAt(LocalDateTime.now());

        AnalysisReport saved = reportRepository.save(report);
        eventPublisher.publishCompleted(saved);
        return saved;
    }
}
