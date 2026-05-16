package com.contentiq.contentiq.observer;

import com.contentiq.contentiq.model.AnalysisReport;
import com.contentiq.contentiq.model.NotesDocument;
import com.contentiq.contentiq.repository.AnalysisReportRepository;
import com.contentiq.contentiq.repository.NotesDocumentRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.List;
import com.itextpdf.layout.element.ListItem;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Slf4j
@Component
public class PDFGenerationObserver implements AnalysisObserver {

    private final AnalysisReportRepository reportRepository;
    private final NotesDocumentRepository notesRepository;
    private final String outputDir;

    public PDFGenerationObserver(AnalysisReportRepository reportRepository,
                                 NotesDocumentRepository notesRepository,
                                 @Value("${contentiq.pdf.output-dir:./generated-pdfs}") String outputDir) {
        this.reportRepository = reportRepository;
        this.notesRepository = notesRepository;
        this.outputDir = outputDir;
    }

    @Override
    public void onAnalysisCompleted(AnalysisReport report) {
        try {
            ensureOutputDir();
            String pdfPath;
            if ("NOTES".equalsIgnoreCase(report.getReportType())) {
                pdfPath = generateNotesPdf(report);
            } else {
                pdfPath = generateSentimentPdf(report);
            }
            report.setPdfPath(pdfPath);
            reportRepository.save(report);
            log.info("[PDF] Generated PDF for report {} at {}", report.getId(), pdfPath);
        } catch (Exception e) {
            log.error("[PDF] Failed to generate PDF for report {}: {}", report.getId(), e.getMessage(), e);
        }
    }

    private void ensureOutputDir() throws Exception {
        Path dir = Paths.get(outputDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }

    private String generateSentimentPdf(AnalysisReport report) throws Exception {
        String filename = "sentiment_" + report.getId() + ".pdf";
        File file = new File(outputDir, filename);

        try (PdfWriter writer = new PdfWriter(file.getAbsolutePath());
             PdfDocument pdf = new PdfDocument(writer);
             Document doc = new Document(pdf)) {

            doc.add(new Paragraph("ContentIQ - Comment Sentiment Report")
                    .setFontSize(20).setBold().setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Report ID: " + report.getId()));
            doc.add(new Paragraph("Video ID: " + report.getVideoId()));
            doc.add(new Paragraph("Generated: " + LocalDateTime.now()));
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Statistics").setBold().setFontSize(14));
            List stats = new List();
            stats.add(new ListItem("Total comments: " + nz(report.getTotalItems())));
            stats.add(new ListItem("Positive: " + nz(report.getPositiveCount())));
            stats.add(new ListItem("Negative: " + nz(report.getNegativeCount())));
            stats.add(new ListItem("Neutral: " + nz(report.getNeutralCount())));
            stats.add(new ListItem("Questions: " + nz(report.getQuestionCount())));
            stats.add(new ListItem("Complaints: " + nz(report.getComplaintCount())));
            stats.add(new ListItem("Spam: " + nz(report.getSpamCount())));
            doc.add(stats);

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Summary").setBold().setFontSize(14));
            doc.add(new Paragraph(report.getSummary() == null ? "No summary." : report.getSummary()));
        }
        return file.getAbsolutePath();
    }

    private String generateNotesPdf(AnalysisReport report) throws Exception {
        java.util.List<NotesDocument> docs = notesRepository.findByVideoId(report.getVideoId());
        NotesDocument notes = docs.isEmpty() ? null : docs.get(docs.size() - 1);

        String filename = "notes_" + report.getId() + ".pdf";
        File file = new File(outputDir, filename);

        try (PdfWriter writer = new PdfWriter(file.getAbsolutePath());
             PdfDocument pdf = new PdfDocument(writer);
             Document doc = new Document(pdf)) {

            String title = notes != null && notes.getTitle() != null ? notes.getTitle() : "Video Notes";
            doc.add(new Paragraph(title).setFontSize(22).setBold().setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Report ID: " + report.getId()));
            doc.add(new Paragraph("Video ID: " + report.getVideoId()));
            doc.add(new Paragraph("Generated: " + LocalDateTime.now()));
            doc.add(new Paragraph(" "));

            if (notes != null && notes.getOverview() != null) {
                doc.add(new Paragraph("Overview").setBold().setFontSize(14));
                doc.add(new Paragraph(notes.getOverview()));
                doc.add(new Paragraph(" "));
            }

            if (notes != null && notes.getSections() != null) {
                for (NotesDocument.Section s : notes.getSections()) {
                    doc.add(new Paragraph(s.getHeading() == null ? "Section" : s.getHeading())
                            .setBold().setFontSize(14));
                    doc.add(new Paragraph(s.getContent() == null ? "" : s.getContent()));
                    doc.add(new Paragraph(" "));
                }
            }

            if (notes != null && notes.getKeyTakeaways() != null && !notes.getKeyTakeaways().isEmpty()) {
                doc.add(new AreaBreak());
                doc.add(new Paragraph("Key Takeaways").setBold().setFontSize(14));
                List takeaways = new List();
                for (String t : notes.getKeyTakeaways()) {
                    takeaways.add(new ListItem(t));
                }
                doc.add(takeaways);
            }

            if (notes != null && notes.getActionItems() != null && !notes.getActionItems().isEmpty()) {
                doc.add(new Paragraph(" "));
                doc.add(new Paragraph("Action Items").setBold().setFontSize(14));
                List actions = new List();
                for (String a : notes.getActionItems()) {
                    actions.add(new ListItem(a));
                }
                doc.add(actions);
            }
        }

        if (notes != null) {
            notes.setPdfPath(file.getAbsolutePath());
            notesRepository.save(notes);
        }
        return file.getAbsolutePath();
    }

    private int nz(Integer v) {
        return v == null ? 0 : v;
    }
}
