package com.brandonkamga.sourcefolio.engagement.domain.port;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** When each author last read the feedback they received. */
public interface FeedbackReadStore {

    Optional<Instant> seenAt(UUID memberId);

    void markSeen(UUID memberId, Instant at);
}
