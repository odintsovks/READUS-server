package com.readus.forum.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class DiscussionResponse {
    private UUID id;
    private UUID branchId;
    private String title;
    private String slug;
    private String content;
    private int viewCount;
    private int replyCount;
    private int reactionCount;
    private Instant createdAt;
    private UserInfo user;

    @Data
    @Builder
    public static class UserInfo {
        private UUID id;
        private String username;
        private String avatarUrl;
    }
}