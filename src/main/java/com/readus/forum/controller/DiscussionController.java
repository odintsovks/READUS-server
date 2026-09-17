package com.readus.forum.controller;

import com.readus.forum.dto.*;
import com.readus.forum.service.DiscussionService;
import com.readus.forum.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/discussions")
@RequiredArgsConstructor
public class DiscussionController {

    private final DiscussionService discussionService;

    @GetMapping
    public ResponseEntity<CursorPage<DiscussionResponse>> getAll(
            @RequestParam(value = "branch_id", required = false) UUID branchId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(discussionService.getAll(branchId, cursor, limit));
    }

    @GetMapping("/{idOrSlug}")
    public ResponseEntity<DiscussionDetailResponse> getByIdOrSlug(@PathVariable String idOrSlug) {
        return ResponseEntity.ok(discussionService.getByIdOrSlug(idOrSlug));
    }

    @PostMapping
    public ResponseEntity<DiscussionResponse> create(@Valid @RequestBody CreateDiscussionRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.status(201).body(discussionService.create(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiscussionResponse> update(@PathVariable UUID id,
                                                     @Valid @RequestBody DiscussionUpdateRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(discussionService.update(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        discussionService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
