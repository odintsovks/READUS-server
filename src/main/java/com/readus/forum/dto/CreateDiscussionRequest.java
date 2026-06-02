package com.readus.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateDiscussionRequest {
    @NotBlank
    @Size(max = 150)
    private String title;

    @NotBlank
    @Size(max = 10000)
    private String content;
}