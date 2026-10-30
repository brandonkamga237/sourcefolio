package com.brandonkamga.sourcefolio.showcase.application;

import java.time.Instant;
import java.util.UUID;

/** Published once a showcase is published. The catalogue lists it from this alone. */
public record ShowcasePublished(UUID showcaseId, String slug, String siteUrl, String repository, String repositoryUrl,
                                UUID ownerId, String ownerLogin, Instant publishedAt) {
}
