package com.readus.forum.entity;

import java.time.Instant;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "uploads")
@Getter
@Setter
public class Upload extends BaseEntity {
    private String originalName;

    @Column(unique = true)
    private String storedPath;

    private Integer mediaType;

    private Integer processingStatus = 0;

    private String previewPath;

    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private Message message;
    
    @UpdateTimestamp
    private Instant updatedAt;
}
