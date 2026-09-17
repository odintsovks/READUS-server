package com.readus.forum.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * A logged user activity event (page views, clicks, ...) used for feed ranking.
 * {@code userId} is nullable: the client may send a placeholder before login.
 */
@Entity
@Table(name = "analytics_events")
@Getter
@Setter
public class AnalyticsEvent extends BaseEntity {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "page_url", columnDefinition = "TEXT")
    private String pageUrl;
}
