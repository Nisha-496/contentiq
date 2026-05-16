package com.contentiq.contentiq.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notes_documents")
public class NotesDocument {

    @Id
    private String id;

    @Indexed
    private String videoId;

    private String title;
    private String overview;

    @Builder.Default
    private List<Section> sections = new ArrayList<>();

    @Builder.Default
    private List<String> keyTakeaways = new ArrayList<>();

    @Builder.Default
    private List<String> actionItems = new ArrayList<>();

    private String pdfPath;
    private String status;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Section {
        private String heading;
        private String content;
        private Integer order;
    }
}
