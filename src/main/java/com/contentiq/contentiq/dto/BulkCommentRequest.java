package com.contentiq.contentiq.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkCommentRequest {

    @NotBlank
    private String videoId;

    @NotEmpty
    @Valid
    private List<CommentRequest> comments;
}
