package com.brandonkamga.sourcefolio.discovery.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * One published showcase as the catalogue shows it. A copy, fed by events from showcase and
 * engagement; the like count is shown on the card, never used to order Discover.
 */
public record CatalogEntry(UUID showcaseId, String slug, String siteUrl, String repository, String repositoryUrl,
                           UUID ownerId, String ownerLogin, Instant publishedAt, String previewImageUrl, String previewVideoUrl,
                           Integer repositoryStars, int likes) {

    /** An entry as first published: no like yet. */
    public CatalogEntry(UUID showcaseId, String slug, String siteUrl, String repository, String repositoryUrl,
                        UUID ownerId, String ownerLogin, Instant publishedAt, String previewImageUrl, String previewVideoUrl,
                        Integer repositoryStars) {
        this(showcaseId, slug, siteUrl, repository, repositoryUrl, ownerId, ownerLogin, publishedAt, previewImageUrl, previewVideoUrl,
                repositoryStars, 0);
    }

    /** An entry whose repository stars are not known yet. */
    public CatalogEntry(UUID showcaseId, String slug, String siteUrl, String repository, String repositoryUrl,
                        UUID ownerId, String ownerLogin, Instant publishedAt, String previewImageUrl, String previewVideoUrl) {
        this(showcaseId, slug, siteUrl, repository, repositoryUrl, ownerId, ownerLogin, publishedAt, previewImageUrl, previewVideoUrl, null);
    }

    /** An entry whose author has not chosen a preview. */
    public CatalogEntry(UUID showcaseId, String slug, String siteUrl, String repository, String repositoryUrl,
                        UUID ownerId, String ownerLogin, Instant publishedAt) {
        this(showcaseId, slug, siteUrl, repository, repositoryUrl, ownerId, ownerLogin, publishedAt, null, null);
    }

    public CatalogEntry {
        Objects.requireNonNull(showcaseId, "showcaseId");
        Objects.requireNonNull(slug, "slug");
        Objects.requireNonNull(siteUrl, "siteUrl");
        Objects.requireNonNull(ownerId, "ownerId");
        Objects.requireNonNull(publishedAt, "publishedAt");
    }
}
