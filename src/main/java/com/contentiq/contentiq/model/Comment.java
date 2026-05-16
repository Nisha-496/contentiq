package com.contentiq.contentiq.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "comments")
public class Comment {

    @Id
    private String id;

    @Indexed
    private String videoId;

    private String author;
    private String text;
    private Integer likeCount;
    private LocalDateTime publishedAt;

    private String sentiment;
    private String category;
    private Double confidence;
    private Boolean analyzed;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
