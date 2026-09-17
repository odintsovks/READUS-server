package com.readus.forum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/** One ranked feed entry. No-arg + all-args ctors are required for Redis JSON (de)serialization. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedItemResponse {
    /** Always {@code "DISCUSSION"} today; kept as a string for future entity types. */
    private String type;
    private UUID id;
    private String title;
    /** Sanitized HTML rendered from the discussion content (markdown → safe HTML). */
    private String contentHtml;
    private Double relevanceScore;
    private Author author;
    private Instant createdAt;
    private Integer viewCount;
    private Integer replyCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Author {
        private UUID id;
        private String username;
    }
}
