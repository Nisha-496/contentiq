package com.contentiq.contentiq.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YouTubeImportRequest {

    @NotBlank
    private String urlOrId;

    private String transcript;

    private Long maxComments;
}
