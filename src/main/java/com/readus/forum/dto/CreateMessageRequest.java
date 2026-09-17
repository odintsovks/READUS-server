package com.readus.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateMessageRequest {
    @NotNull
    private UUID discussionId;

    /** Null for a root reply. */
    private UUID parentMessageId;

    @NotBlank
    @Size(max = 10000)
    private String content;
}
