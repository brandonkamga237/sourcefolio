package com.brandonkamga.sourcefolio.showcase.application;

import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateScan;

import java.util.Optional;
import java.util.UUID;

public interface ShowcaseDetection {

    /** Asks for a detection in the background; called after every sign-in. */
    void requestDetection(UUID memberId, String login);

    /** Runs a detection now: the background job's entry point. Running it twice gives the same result. */
    void detect(UUID memberId, String login);

    /** The last detection, or empty when none has finished yet. */
    Optional<CandidateScan> findCandidates(UUID memberId);
}
