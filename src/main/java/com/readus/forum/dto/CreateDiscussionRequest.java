package com.readus.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateDiscussionRequest {
    @NotNull
    private UUID branchId;

    @NotBlank
    @Size(max = 150)
    private String title;

    @NotBlank
    @Size(max = 10000)
    private String content;

    /** Optional opening post; when present it is stored as the first root message. */
    @Size(max = 10000)
    private String messageContent;
}
