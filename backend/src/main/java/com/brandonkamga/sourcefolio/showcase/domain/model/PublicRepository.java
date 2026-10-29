package com.brandonkamga.sourcefolio.showcase.domain.model;

import java.time.Instant;

/** What detection needs to know about one of a member's public repositories. */
public record PublicRepository(RepositoryCoordinates coordinates, String homepage, boolean hasPages, Instant pushedAt) {
}
