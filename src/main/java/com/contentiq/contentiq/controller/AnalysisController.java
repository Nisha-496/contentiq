package com.contentiq.contentiq.controller;

import com.contentiq.contentiq.model.AnalysisReport;
import com.contentiq.contentiq.service.AIAnalysisService;
import com.contentiq.contentiq.service.NotesGeneratorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AIAnalysisService aiAnalysisService;
    private final NotesGeneratorService notesGeneratorService;

    public AnalysisController(AIAnalysisService aiAnalysisService,
                              NotesGeneratorService notesGeneratorService) {
        this.aiAnalysisService = aiAnalysisService;
        this.notesGeneratorService = notesGeneratorService;
    }

    @PostMapping("/comments/{videoId}")
    public ResponseEntity<?> analyzeComments(@PathVariable String videoId) {
        AnalysisReport report = aiAnalysisService.analyzeComments(videoId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(report);
    }

    @PostMapping("/notes/{videoId}")
    public ResponseEntity<?> generateNotes(@PathVariable String videoId) {
        AnalysisReport report = notesGeneratorService.generate(videoId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(report);
    }

    @GetMapping("/report/{reportId}")
    public ResponseEntity<?> getReport(@PathVariable String reportId) {
        return ResponseEntity.ok(aiAnalysisService.getReport(reportId));
    }

    @GetMapping("/video/{videoId}/reports")
    public ResponseEntity<?> getReportsForVideo(@PathVariable String videoId) {
        return ResponseEntity.ok(aiAnalysisService.getReportsForVideo(videoId));
    }
}
