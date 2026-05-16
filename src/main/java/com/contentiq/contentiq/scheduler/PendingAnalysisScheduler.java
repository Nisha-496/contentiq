package com.contentiq.contentiq.scheduler;

import com.contentiq.contentiq.model.AnalysisReport;
import com.contentiq.contentiq.repository.AnalysisReportRepository;
import com.contentiq.contentiq.service.AIAnalysisService;
import com.contentiq.contentiq.service.NotesGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class PendingAnalysisScheduler {

    private final AnalysisReportRepository reportRepository;
    private final AIAnalysisService aiAnalysisService;
    private final NotesGeneratorService notesGeneratorService;

    public PendingAnalysisScheduler(AnalysisReportRepository reportRepository,
                                    AIAnalysisService aiAnalysisService,
                                    NotesGeneratorService notesGeneratorService) {
        this.reportRepository = reportRepository;
        this.aiAnalysisService = aiAnalysisService;
        this.notesGeneratorService = notesGeneratorService;
    }

    @Scheduled(fixedDelayString = "${contentiq.scheduler.pending-interval-ms:300000}")
    public void retryStuckReports() {
        List<AnalysisReport> inProgress = reportRepository.findByStatus("IN_PROGRESS");
        if (inProgress.isEmpty()) {
            return;
        }
        LocalDateTime cutoff = LocalDateTime.now().minus(Duration.ofMinutes(10));
        for (AnalysisReport report : inProgress) {
            if (report.getCreatedAt() != null && report.getCreatedAt().isBefore(cutoff)) {
                log.info("[Scheduler] Retrying stuck report {} ({})", report.getId(), report.getReportType());
                try {
                    if ("NOTES".equalsIgnoreCase(report.getReportType())) {
                        notesGeneratorService.generate(report.getVideoId());
                    } else {
                        aiAnalysisService.analyzeComments(report.getVideoId());
                    }
                    report.setStatus("RETRIED");
                    reportRepository.save(report);
                } catch (Exception e) {
                    log.error("[Scheduler] Retry failed for report {}: {}", report.getId(), e.getMessage());
                }
            }
        }
    }
}
