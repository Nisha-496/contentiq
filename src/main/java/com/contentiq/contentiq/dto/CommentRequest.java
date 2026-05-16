package com.contentiq.contentiq.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {

    private String author;

    @NotBlank
    private String text;

    private Integer likeCount;
    private LocalDateTime publishedAt;
}
