package com.readus.forum.controller;

import com.readus.forum.dto.*;
import com.readus.forum.service.DiscussionService;
import com.readus.forum.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/discussions")
@RequiredArgsConstructor
public class DiscussionController {

    private final DiscussionService discussionService;

    @GetMapping
    public ResponseEntity<Page<DiscussionResponse>> getAll(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(discussionService.getAll(pageable));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<DiscussionDetailResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(discussionService.getBySlug(slug));
    }

    @PostMapping
    public ResponseEntity<DiscussionResponse> create(@Valid @RequestBody CreateDiscussionRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(discussionService.create(userId, request));
    }

    @PostMapping("/{discussionId}/messages")
    public ResponseEntity<MessageResponse> addMessage(
            @PathVariable UUID discussionId,
            @Valid @RequestBody CreateMessageRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(discussionService.addMessage(discussionId, userId, request));
    }
}