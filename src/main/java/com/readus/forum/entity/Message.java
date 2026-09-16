package com.readus.forum.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_discussion_id", columnList = "discussion_id"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_created_at", columnList = "created_at DESC")
})
@Getter
@Setter
public class Message extends BaseEntity {
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private String contentHtml;

    private Short moderationStatus = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discussion_id")
    private Discussion discussion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_message_id")
    private Message parentMessage;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL)
    private List<Upload> uploads = new ArrayList<>();

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL)
    private List<Reaction> reactions = new ArrayList<>();

    @UpdateTimestamp
    private Instant updatedAt;
}
