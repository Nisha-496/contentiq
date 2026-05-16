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
public class VideoRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String url;

    private String channelName;
    private String description;

    @NotBlank
    private String transcript;

    private Long durationSeconds;
}
