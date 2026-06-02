package com.readus.forum.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class MessageResponse {
    private UUID id;
    private String content;
    private String contentHtml;
    private Instant createdAt;
    private DiscussionResponse.UserInfo user;
    private int reactionCount;
    private boolean likedByCurrentUser;
}