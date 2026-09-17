package com.readus.forum.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class MessageResponse {
    private UUID id;
    private UUID discussionId;
    private UUID parentMessageId;
    private String content;
    private String contentHtml;
    private Instant createdAt;
    private DiscussionResponse.UserInfo author;
    private int likeCount;
    private int dislikeCount;

    /** Reaction types the current viewer has on this message (empty when anonymous). */
    private List<Short> myReactions;
}
