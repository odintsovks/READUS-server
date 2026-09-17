package com.readus.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

/**
 * Analytics ingestion payload. {@code userId} is a plain String because the client may send
 * a placeholder (e.g. "current-user") before authentication; it is ignored when unparseable.
 */
@Data
public class AnalyticsEventRequest {
    private String userId;

    @NotBlank
    @Size(max = 30)
    private String eventType;

    private UUID entityId;

    private String pageUrl;
}
