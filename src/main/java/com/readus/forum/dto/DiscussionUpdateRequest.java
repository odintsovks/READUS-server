package com.readus.forum.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/** Partial update: only the non-blank fields are applied. */
@Data
public class DiscussionUpdateRequest {
    @Size(max = 150)
    private String title;

    @Size(max = 10000)
    private String content;
}
