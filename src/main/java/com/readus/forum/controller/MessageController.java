package com.readus.forum.controller;

import com.readus.forum.dto.CreateMessageRequest;
import com.readus.forum.dto.MessageResponse;
import com.readus.forum.dto.MessageUpdateRequest;
import com.readus.forum.service.MessageService;
import com.readus.forum.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/discussions/{discussionId}/messages")
    public ResponseEntity<List<MessageResponse>> listByDiscussion(@PathVariable UUID discussionId) {
        return ResponseEntity.ok(messageService.listByDiscussion(discussionId));
    }

    @PostMapping("/messages")
    public ResponseEntity<MessageResponse> create(@Valid @RequestBody CreateMessageRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.status(201).body(messageService.create(userId, request));
    }

    @PutMapping("/messages/{id}")
    public ResponseEntity<MessageResponse> update(@PathVariable UUID id,
                                                  @Valid @RequestBody MessageUpdateRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(messageService.update(userId, id, request));
    }
}
