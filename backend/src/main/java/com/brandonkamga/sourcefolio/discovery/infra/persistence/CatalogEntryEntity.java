package com.brandonkamga.sourcefolio.discovery.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

// Read here, written by native upserts in CatalogEntryJpaRepository.
@Entity
@Table(name = "catalog_entries")
class CatalogEntryEntity {

    @Id
    @Column(name = "showcase_id")
    private UUID showcaseId;

    @Column(nullable = false)
    private String slug;

    @Column(name = "site_url", nullable = false)
    private String siteUrl;

    @Column(nullable = false)
    private String repository;

    @Column(name = "repository_url", nullable = false)
    private String repositoryUrl;

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

    @Column(name = "owner_login", nullable = false)
    private String ownerLogin;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;

    protected CatalogEntryEntity() {
    }

    UUID getShowcaseId() { return showcaseId; }
    String getSlug() { return slug; }
    String getSiteUrl() { return siteUrl; }
    String getRepository() { return repository; }
    String getRepositoryUrl() { return repositoryUrl; }
    UUID getOwnerUserId() { return ownerUserId; }
    String getOwnerLogin() { return ownerLogin; }
    Instant getPublishedAt() { return publishedAt; }
}
