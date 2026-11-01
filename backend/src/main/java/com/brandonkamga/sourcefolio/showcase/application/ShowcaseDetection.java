package com.brandonkamga.sourcefolio.showcase.application;

import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateScan;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public interface ShowcaseDetection {

    Duration RESCAN_INTERVAL = Duration.ofSeconds(30);

    /** Asks for a detection in the background; called after every sign-in. */
    void requestDetection(UUID memberId, String login);

    /**
     * Searches the member's repositories again, in the background, from Me: after they fill a
     * repository's "Website" field on GitHub, or before changing portfolio. At most once every
     * {@link #RESCAN_INTERVAL}, to spare the GitHub quota.
     */
    void requestRescan(UUID memberId);

    /** Runs a detection now: the background job's entry point. Running it twice gives the same result. */
    void detect(UUID memberId, String login);

    /** The last detection, or empty when none has finished yet. */
    Optional<CandidateScan> findCandidates(UUID memberId);
}
