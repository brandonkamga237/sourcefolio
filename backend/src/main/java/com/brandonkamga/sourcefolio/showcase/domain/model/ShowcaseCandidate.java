package com.brandonkamga.sourcefolio.showcase.domain.model;

/** A repository and the site it seems to publish: what the "Is this your portfolio?" card shows. */
public record ShowcaseCandidate(RepositoryCoordinates repository, SiteUrl siteUrl, CandidateReason reason) {
}
