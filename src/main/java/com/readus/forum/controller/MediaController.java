package com.readus.forum.controller;

import com.readus.forum.dto.MediaUploadResponse;
import com.readus.forum.entity.Upload;
import com.readus.forum.service.MediaService;
import com.readus.forum.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@Slf4j
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/upload")
    public ResponseEntity<List<MediaUploadResponse>> uploadFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("messageId") UUID messageId) {

        UUID userId = SecurityUtils.getCurrentUserId();
        List<MediaUploadResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                Upload upload = mediaService.uploadFile(file, userId, messageId);
                responses.add(MediaUploadResponse.builder()
                        .id(upload.getId())
                        .originalName(upload.getOriginalName())
                        .storedPath(upload.getStoredPath())
                        .previewPath(upload.getPreviewPath())
                        .mediaType(upload.getMediaType())
                        .processingStatus(upload.getProcessingStatus())
                        .build());
            } catch (Exception e) {
                log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
                throw new RuntimeException("Upload failed: " + e.getMessage());
            }
        }

        return ResponseEntity.ok(responses);
    }
}
