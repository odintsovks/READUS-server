package com.readus.forum.controller;

import com.readus.forum.dto.CursorPage;
import com.readus.forum.dto.FeedItemResponse;
import com.readus.forum.service.FeedService;
import com.readus.forum.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    public ResponseEntity<CursorPage<FeedItemResponse>> getFeed(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(feedService.getPersonalizedFeed(userId, cursor, limit));
    }
}
