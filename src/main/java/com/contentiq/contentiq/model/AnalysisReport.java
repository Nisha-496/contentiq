package com.contentiq.contentiq.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "analysis_reports")
public class AnalysisReport {

    @Id
    private String id;

    @Indexed
    private String videoId;

    private String reportType;
    private String status;

    private Integer totalItems;
    private Integer positiveCount;
    private Integer negativeCount;
    private Integer neutralCount;
    private Integer questionCount;
    private Integer complaintCount;
    private Integer spamCount;

    private String summary;
    private String pdfPath;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    private String errorMessage;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime completedAt;
}
