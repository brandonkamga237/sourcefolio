package com.brandonkamga.sourcefolio.showcase.domain.model;

import java.time.Instant;
import java.util.List;

/** The result of the last detection for a member: when it ran, and what it found, best first. */
public record CandidateScan(Instant scannedAt, List<ShowcaseCandidate> candidates) {

    public CandidateScan {
        candidates = List.copyOf(candidates);
    }
}
