package com.brandonkamga.sourcefolio.showcase.domain.port;

import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateScan;

import java.util.Optional;
import java.util.UUID;

public interface CandidateStore {

    /** Replaces the member's previous scan, atomically: a reader never sees half of one. */
    void replace(UUID memberId, CandidateScan scan);

    Optional<CandidateScan> find(UUID memberId);
}
