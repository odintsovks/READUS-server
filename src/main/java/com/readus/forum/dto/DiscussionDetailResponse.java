package com.readus.forum.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class DiscussionDetailResponse {
    private UUID id;
    private UUID branchId;
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

    /** Preview of the most recent message (null for discussions without messages). */
    private MessageResponse lastMessage;
}
