package com.readus.forum.controller;

import com.readus.forum.dto.ReactionCreateRequest;
import com.readus.forum.dto.ReactionResponse;
import com.readus.forum.service.ReactionService;
import com.readus.forum.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;

    @PostMapping("/discussions/{discussionId}/reactions")
    public ResponseEntity<ReactionResponse> reactToDiscussion(@PathVariable UUID discussionId,
                                                              @Valid @RequestBody ReactionCreateRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(reactionService.reactToDiscussion(userId, discussionId, request.getType()));
    }

    @PostMapping("/messages/{messageId}/reactions")
    public ResponseEntity<ReactionResponse> reactToMessage(@PathVariable UUID messageId,
                                                           @Valid @RequestBody ReactionCreateRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(reactionService.reactToMessage(userId, messageId, request.getType()));
    }

    @DeleteMapping("/discussions/{discussionId}/reactions/{reactionType}")
    public ResponseEntity<Void> removeDiscussionReaction(@PathVariable UUID discussionId,
                                                         @PathVariable Short reactionType) {
        UUID userId = SecurityUtils.getCurrentUserId();
        reactionService.removeDiscussionReaction(userId, discussionId, reactionType);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/messages/{messageId}/reactions/{reactionType}")
    public ResponseEntity<Void> removeMessageReaction(@PathVariable UUID messageId,
                                                      @PathVariable Short reactionType) {
        UUID userId = SecurityUtils.getCurrentUserId();
        reactionService.removeMessageReaction(userId, messageId, reactionType);
        return ResponseEntity.noContent().build();
    }
}
