package com.brandonkamga.sourcefolio.discovery.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * One published showcase as the catalogue shows it. A copy, fed by events from showcase and
 * engagement, and the author's public profile (country, ranked specialties, headline, declared
 * role) from profile; the like count is shown on the card, never used to order Discover.
 */
public record CatalogEntry(UUID showcaseId, String slug, String siteUrl, String repository, String repositoryUrl,
                           UUID ownerId, String ownerLogin, Instant publishedAt, String previewImageUrl, String previewVideoUrl,
                           Integer repositoryStars, int likes, String countryCode, List<String> specialties, String headline,
                           String repositoryDescription, List<String> repositoryLanguages, Instant repositoryPushedAt,
                           String authorRole) {

    /** An entry whose author has declared no role. */
    public CatalogEntry(UUID showcaseId, String slug, String siteUrl, String repository, String repositoryUrl,
                        UUID ownerId, String ownerLogin, Instant publishedAt, String previewImageUrl, String previewVideoUrl,
                        Integer repositoryStars, int likes, String countryCode, List<String> specialties, String headline,
                        String repositoryDescription, List<String> repositoryLanguages, Instant repositoryPushedAt) {
        this(showcaseId, slug, siteUrl, repository, repositoryUrl, ownerId, ownerLogin, publishedAt, previewImageUrl, previewVideoUrl,
                repositoryStars, likes, countryCode, specialties, headline, repositoryDescription, repositoryLanguages, repositoryPushedAt,
                null);
    }

    /** An entry whose repository GitHub has not described yet. */
    public CatalogEntry(UUID showcaseId, String slug, String siteUrl, String repository, String repositoryUrl,
                        UUID ownerId, String ownerLogin, Instant publishedAt, String previewImageUrl, String previewVideoUrl,
                        Integer repositoryStars, int likes, String countryCode, List<String> specialties, String headline) {
        this(showcaseId, slug, siteUrl, repository, repositoryUrl, ownerId, ownerLogin, publishedAt, previewImageUrl, previewVideoUrl,
                repositoryStars, likes, countryCode, specialties, headline, null, List.of(), null);
    }

    /** An entry whose author has written no headline. */
    public CatalogEntry(UUID showcaseId, String slug, String siteUrl, String repository, String repositoryUrl,
                        UUID ownerId, String ownerLogin, Instant publishedAt, String previewImageUrl, String previewVideoUrl,
                        Integer repositoryStars, int likes, String countryCode, List<String> specialties) {
        this(showcaseId, slug, siteUrl, repository, repositoryUrl, ownerId, ownerLogin, publishedAt, previewImageUrl, previewVideoUrl,
                repositoryStars, likes, countryCode, specialties, null);
    }

    /** An entry whose author's profile is not copied yet. */
    public CatalogEntry(UUID showcaseId, String slug, String siteUrl, String repository, String repositoryUrl,
                        UUID ownerId, String ownerLogin, Instant publishedAt, String previewImageUrl, String previewVideoUrl,
                        Integer repositoryStars, int likes) {
        this(showcaseId, slug, siteUrl, repository, repositoryUrl, ownerId, ownerLogin, publishedAt, previewImageUrl, previewVideoUrl,
                repositoryStars, likes, null, List.of());
    }

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
        specialties = specialties == null ? List.of() : List.copyOf(specialties);
        repositoryLanguages = repositoryLanguages == null ? List.of() : List.copyOf(repositoryLanguages);
    }
}
