package com.readus.forum.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class DiscussionDetailResponse {
    private UUID id;
    private String title;
    private String slug;
    private String content;
    private String contentHtml;
    private int viewCount;
    private int replyCount;
    private int reactionCount;
    private Instant createdAt;
    private Instant updatedAt;
    private DiscussionResponse.UserInfo user;
    private List<MessageResponse> messages;
}