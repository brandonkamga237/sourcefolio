package com.brandonkamga.sourcefolio.discovery.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Column(name = "preview_image_url", length = 1000)
    private String previewImageUrl;

    @Column(name = "preview_video_url", length = 1000)
    private String previewVideoUrl;

    @Column(name = "repository_stars")
    private Integer repositoryStars;

    // CHAR(2) in the schema: without this, Hibernate validates it as a VARCHAR and refuses to start.
    @Column(name = "country_code", length = 2)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String countryCode;

    // VARCHAR(20)[] in the schema, most important first.
    @Column(name = "specialties", nullable = false, columnDefinition = "varchar(20)[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private String[] specialties = new String[0];

    @Column(length = 80)
    private String headline;

    @Column(name = "repository_description", length = 350)
    private String repositoryDescription;

    // VARCHAR(40)[] in the schema, largest share first.
    @Column(name = "repository_languages", nullable = false, columnDefinition = "varchar(40)[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private String[] repositoryLanguages = new String[0];

    @Column(name = "repository_pushed_at")
    private Instant repositoryPushedAt;

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
    int getLikeCount() { return likeCount; }
    String getPreviewImageUrl() { return previewImageUrl; }
    String getPreviewVideoUrl() { return previewVideoUrl; }
    Integer getRepositoryStars() { return repositoryStars; }
    String getCountryCode() { return countryCode; }
    String[] getSpecialties() { return specialties; }
    String getHeadline() { return headline; }
    String getRepositoryDescription() { return repositoryDescription; }
    String[] getRepositoryLanguages() { return repositoryLanguages; }
    Instant getRepositoryPushedAt() { return repositoryPushedAt; }
}
