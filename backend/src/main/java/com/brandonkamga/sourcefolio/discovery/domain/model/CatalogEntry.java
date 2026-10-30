package com.brandonkamga.sourcefolio.discovery.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** One published showcase as the catalogue shows it. A copy, fed by events from showcase. */
public record CatalogEntry(UUID showcaseId, String slug, String siteUrl, String repository, String repositoryUrl,
                           UUID ownerId, String ownerLogin, Instant publishedAt) {

    public CatalogEntry {
        Objects.requireNonNull(showcaseId, "showcaseId");
        Objects.requireNonNull(slug, "slug");
        Objects.requireNonNull(siteUrl, "siteUrl");
        Objects.requireNonNull(ownerId, "ownerId");
        Objects.requireNonNull(publishedAt, "publishedAt");
    }
}
