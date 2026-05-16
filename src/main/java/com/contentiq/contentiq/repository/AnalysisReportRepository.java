package com.contentiq.contentiq.repository;

import com.contentiq.contentiq.model.AnalysisReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalysisReportRepository extends MongoRepository<AnalysisReport, String> {

    List<AnalysisReport> findByVideoId(String videoId);

    List<AnalysisReport> findByStatus(String status);

    List<AnalysisReport> findByVideoIdAndReportType(String videoId, String reportType);
}
