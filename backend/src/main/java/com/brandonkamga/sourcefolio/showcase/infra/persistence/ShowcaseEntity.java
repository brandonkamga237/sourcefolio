package com.brandonkamga.sourcefolio.showcase.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "showcases")
class ShowcaseEntity {

    @Id
    private UUID id;

    @Column(name = "owner_user_id", nullable = false, updatable = false)
    private UUID ownerUserId;

    @Column(nullable = false, length = 20)
    private String kind;

    @Column(name = "site_url", nullable = false, length = 1000)
    private String siteUrl;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    protected ShowcaseEntity() {
    }

    ShowcaseEntity(UUID id, UUID ownerUserId, Instant createdAt) {
        this.id = id;
        this.ownerUserId = ownerUserId;
        this.createdAt = createdAt;
    }

    void update(String kind, String siteUrl, String status, Instant publishedAt) {
        this.kind = kind;
        this.siteUrl = siteUrl;
        this.status = status;
        this.publishedAt = publishedAt;
    }

    UUID getId() { return id; }
    UUID getOwnerUserId() { return ownerUserId; }
    String getKind() { return kind; }
    String getSiteUrl() { return siteUrl; }
    String getStatus() { return status; }
    Instant getCreatedAt() { return createdAt; }
    Instant getPublishedAt() { return publishedAt; }
}
