package com.brandonkamga.sourcefolio.showcase.api.dto;

import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateScan;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseCandidate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** scannedAt is null until the first detection finishes: "scanning" and "nothing found" differ. */
public record ShowcaseCandidatesResponse(Instant scannedAt, List<Candidate> candidates) {

    public record Candidate(String repository, String repositoryUrl, String siteUrl, String reason) {

        static Candidate of(ShowcaseCandidate candidate) {
            return new Candidate(candidate.repository().toString(), "https://github.com/" + candidate.repository(),
                    candidate.siteUrl().value(), candidate.reason().name());
        }
    }

    public static ShowcaseCandidatesResponse of(Optional<CandidateScan> scan) {
        return scan.map(found -> new ShowcaseCandidatesResponse(found.scannedAt(), found.candidates().stream().map(Candidate::of).toList()))
                .orElseGet(() -> new ShowcaseCandidatesResponse(null, List.of()));
    }
}
