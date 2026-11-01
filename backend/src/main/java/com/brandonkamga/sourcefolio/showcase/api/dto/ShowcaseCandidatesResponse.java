package com.brandonkamga.sourcefolio.showcase.api.dto;

import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateScan;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseCandidate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * scannedAt is null until the first detection finishes: "scanning" and "nothing found" differ.
 * withoutSite lists the member's other repositories, which become candidates once their GitHub
 * "Website" field is filled.
 */
public record ShowcaseCandidatesResponse(Instant scannedAt, List<Candidate> candidates, List<Repository> withoutSite) {

    public record Repository(String repository, String repositoryUrl) {

        static Repository of(RepositoryCoordinates coordinates) {
            return new Repository(coordinates.toString(), "https://github.com/" + coordinates);
        }
    }

    public record Candidate(String repository, String repositoryUrl, String siteUrl, String reason) {

        static Candidate of(ShowcaseCandidate candidate) {
            return new Candidate(candidate.repository().toString(), "https://github.com/" + candidate.repository(),
                    candidate.siteUrl().value(), candidate.reason().name());
        }
    }

    public static ShowcaseCandidatesResponse of(Optional<CandidateScan> scan) {
        return scan.map(found -> new ShowcaseCandidatesResponse(found.scannedAt(), found.candidates().stream().map(Candidate::of).toList(),
                        found.withoutSite().stream().map(Repository::of).toList()))
                .orElseGet(() -> new ShowcaseCandidatesResponse(null, List.of(), List.of()));
    }
}
