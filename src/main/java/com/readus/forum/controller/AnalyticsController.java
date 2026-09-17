package com.readus.forum.controller;

import com.readus.forum.dto.AnalyticsEventRequest;
import com.readus.forum.entity.AnalyticsEvent;
import com.readus.forum.repository.AnalyticsEventRepository;
import com.readus.forum.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsEventRepository analyticsEventRepository;

    @PostMapping("/events")
    public ResponseEntity<Void> ingest(@Valid @RequestBody AnalyticsEventRequest request) {
        AnalyticsEvent event = new AnalyticsEvent();
        event.setUserId(resolveUserId(request.getUserId()));
        event.setEventType(request.getEventType());
        event.setEntityId(request.getEntityId());
        event.setPageUrl(request.getPageUrl());
        analyticsEventRepository.save(event);
        return ResponseEntity.accepted().build();
    }

    /** Prefer the authenticated identity; fall back to parsing the body value, else null. */
    private UUID resolveUserId(String raw) {
        UUID authenticated = SecurityUtils.getOptionalCurrentUserId();
        if (authenticated != null) {
            return authenticated;
        }
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
