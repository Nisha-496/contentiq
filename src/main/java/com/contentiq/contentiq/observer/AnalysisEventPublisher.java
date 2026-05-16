package com.contentiq.contentiq.observer;

import com.contentiq.contentiq.model.AnalysisReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class AnalysisEventPublisher {

    private final List<AnalysisObserver> observers;

    public AnalysisEventPublisher(List<AnalysisObserver> observers) {
        this.observers = observers;
        log.info("AnalysisEventPublisher registered {} observers", observers.size());
    }

    public void publishCompleted(AnalysisReport report) {
        for (AnalysisObserver observer : observers) {
            try {
                observer.onAnalysisCompleted(report);
            } catch (Exception e) {
                log.error("Observer {} failed for report {}: {}",
                        observer.getClass().getSimpleName(), report.getId(), e.getMessage(), e);
            }
        }
    }
}
