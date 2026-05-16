package com.contentiq.contentiq.observer;

import com.contentiq.contentiq.model.AnalysisReport;

public interface AnalysisObserver {

    void onAnalysisCompleted(AnalysisReport report);
}
