package com.readus.forum.entity;

import java.time.Instant;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "profiles")
@Getter
@Setter
public class Profile extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String bio;

    private String location;

    @UpdateTimestamp
    private Instant updatedAt;
}
