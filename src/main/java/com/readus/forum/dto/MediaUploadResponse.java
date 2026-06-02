package com.readus.forum.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MediaUploadResponse {
    private UUID id;
    private String originalName;
    private String storedPath;
    private String previewPath;
    private Integer mediaType;
    private Integer processingStatus;
}