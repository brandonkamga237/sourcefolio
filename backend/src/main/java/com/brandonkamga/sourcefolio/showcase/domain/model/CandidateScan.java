package com.brandonkamga.sourcefolio.showcase.domain.model;

import java.time.Instant;
import java.util.List;

/**
 * The result of the last detection for a member: when it ran, what it found, best first, and
 * their other public repositories, most recently pushed first, for which no site was found.
 */
public record CandidateScan(Instant scannedAt, List<ShowcaseCandidate> candidates, List<RepositoryCoordinates> withoutSite) {

    public CandidateScan {
        candidates = List.copyOf(candidates);
        withoutSite = List.copyOf(withoutSite);
    }

    public CandidateScan(Instant scannedAt, List<ShowcaseCandidate> candidates) {
        this(scannedAt, candidates, List.of());
    }
}
