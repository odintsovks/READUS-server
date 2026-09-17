package com.readus.forum.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * A user's reaction to a discussion or message. Targets are plain columns
 * ({@code target_type} + {@code message_id}/{@code discussion_id}) instead of relations,
 * so one row type covers both targets without lazy-loading overhead. Uniqueness per
 * (user, target, type) is enforced by the partial unique indexes from V3.
 */
@Entity
@Table(name = "reactions")
@Getter
@Setter
public class Reaction extends BaseEntity {
    /** 1 = Like, 2 = Dislike, 3 = Helpful. */
    @Column(nullable = false)
    private Short type;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /** {@code 'MESSAGE'} or {@code 'DISCUSSION'}. */
    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType;

    @Column(name = "message_id")
    private UUID messageId;

    @Column(name = "discussion_id")
    private UUID discussionId;
}
