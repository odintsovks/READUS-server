package com.readus.forum.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ReactionResponse {
    private UUID id;
    private Short type;
    private String targetType;
    private UUID messageId;
    private UUID discussionId;
}
