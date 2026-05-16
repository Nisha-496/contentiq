package com.contentiq.contentiq.observer;

import com.contentiq.contentiq.model.AnalysisReport;
import com.contentiq.contentiq.model.Notification;
import com.contentiq.contentiq.repository.NotificationRepository;
import com.contentiq.contentiq.repository.VideoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationObserver implements AnalysisObserver {

    private final NotificationRepository notificationRepository;
    private final VideoRepository videoRepository;

    public NotificationObserver(NotificationRepository notificationRepository,
                                VideoRepository videoRepository) {
        this.notificationRepository = notificationRepository;
        this.videoRepository = videoRepository;
    }

    @Override
    public void onAnalysisCompleted(AnalysisReport report) {
        String userId = videoRepository.findById(report.getVideoId())
                .map(v -> v.getOwnerUserId())
                .orElse(null);

        String type = "NOTES".equalsIgnoreCase(report.getReportType())
                ? "NOTES_READY" : "ANALYSIS_READY";

        String title = "NOTES".equalsIgnoreCase(report.getReportType())
                ? "Notes generated"
                : "Comment analysis complete";

        String message = "NOTES".equalsIgnoreCase(report.getReportType())
                ? "Your notes document is ready for video " + report.getVideoId()
                : "Analysis processed " + (report.getTotalItems() == null ? 0 : report.getTotalItems())
                  + " comments for video " + report.getVideoId();

        Notification notification = Notification.builder()
                .userId(userId)
                .videoId(report.getVideoId())
                .reportId(report.getId())
                .type(type)
                .title(title)
                .message(message)
                .build();

        notificationRepository.save(notification);
        log.info("[NOTIFY] Saved notification for report {} (user {})", report.getId(), userId);
    }
}
