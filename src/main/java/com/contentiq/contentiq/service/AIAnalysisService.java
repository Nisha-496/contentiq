package com.contentiq.contentiq.service;

import com.contentiq.contentiq.exception.ResourceNotFoundException;
import com.contentiq.contentiq.factory.ContentProcessorFactory;
import com.contentiq.contentiq.model.AnalysisReport;
import com.contentiq.contentiq.model.Comment;
import com.contentiq.contentiq.observer.AnalysisEventPublisher;
import com.contentiq.contentiq.repository.AnalysisReportRepository;
import com.contentiq.contentiq.repository.VideoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AIAnalysisService {

    private final ContentProcessorFactory factory;
    private final CommentService commentService;
    private final AnalysisReportRepository reportRepository;
    private final VideoRepository videoRepository;
    private final AnalysisEventPublisher eventPublisher;

    public AIAnalysisService(ContentProcessorFactory factory,
                             CommentService commentService,
                             AnalysisReportRepository reportRepository,
                             VideoRepository videoRepository,
                             AnalysisEventPublisher eventPublisher) {
        this.factory = factory;
        this.commentService = commentService;
        this.reportRepository = reportRepository;
        this.videoRepository = videoRepository;
        this.eventPublisher = eventPublisher;
    }

    public AnalysisReport analyzeComments(String videoId) {
        if (!videoRepository.existsById(videoId)) {
            throw new ResourceNotFoundException("Video not found: " + videoId);
        }

        AnalysisReport report = AnalysisReport.builder()
                .videoId(videoId)
                .reportType("SENTIMENT")
                .status("IN_PROGRESS")
                .build();
        report = reportRepository.save(report);

        try {
            List<Comment> comments = commentService.getByVideoId(videoId);
            if (comments.isEmpty()) {
                report.setStatus("COMPLETED");
                report.setTotalItems(0);
                report.setSummary("No comments to analyze.");
                report.setCompletedAt(LocalDateTime.now());
                return reportRepository.save(report);
            }

            List<Comment> analyzed = factory.sentiment().analyze(comments);
            commentService.saveAll(analyzed);

            int positive = 0, negative = 0, neutral = 0, questions = 0, complaints = 0, spam = 0;
            for (Comment c : analyzed) {
                switch (nullSafe(c.getSentiment())) {
                    case "POSITIVE" -> positive++;
                    case "NEGATIVE" -> negative++;
                    default -> neutral++;
                }
                switch (nullSafe(c.getCategory())) {
                    case "QUESTION" -> questions++;
                    case "COMPLAINT" -> complaints++;
                    case "SPAM" -> spam++;
                    default -> { }
                }
            }

            String summary = factory.commentSummary().analyze(analyzed);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("strategy", factory.sentiment().getName());
            metadata.put("analyzedCount", analyzed.size());

            report.setStatus("COMPLETED");
            report.setTotalItems(analyzed.size());
            report.setPositiveCount(positive);
            report.setNegativeCount(negative);
            report.setNeutralCount(neutral);
            report.setQuestionCount(questions);
            report.setComplaintCount(complaints);
            report.setSpamCount(spam);
            report.setSummary(summary);
            report.setMetadata(metadata);
            report.setCompletedAt(LocalDateTime.now());

            AnalysisReport saved = reportRepository.save(report);
            eventPublisher.publishCompleted(saved);
            return saved;
        } catch (Exception e) {
            log.error("Comment analysis failed for video {}: {}", videoId, e.getMessage(), e);
            report.setStatus("FAILED");
            report.setErrorMessage(e.getMessage());
            report.setCompletedAt(LocalDateTime.now());
            return reportRepository.save(report);
        }
    }

    public AnalysisReport getReport(String reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + reportId));
    }

    public List<AnalysisReport> getReportsForVideo(String videoId) {
        return reportRepository.findByVideoId(videoId);
    }

    private String nullSafe(String s) {
        return s == null ? "" : s.toUpperCase();
    }
}
