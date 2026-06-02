package com.readus.forum.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "discussions", indexes = {
        @Index(name = "idx_slug", columnList = "slug"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_created_at", columnList = "created_at DESC")
})
@Getter
@Setter
public class Discussion extends BaseEntity {
    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String contentHtml;

    private int viewCount = 0;

    private int replyCount = 0;

    private int reactionCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "discussion", cascade = CascadeType.ALL)
    private List<Message> messages = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_message_id")
    private Message lastMessage;

    @UpdateTimestamp
    private Instant updatedAt;
}
