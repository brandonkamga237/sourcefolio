package com.brandonkamga.sourcefolio.engagement.domain.port;

import java.time.Instant;
import java.util.UUID;

/** Each method is one statement: two taps never count twice, a missing like is never removed. */
public interface LikeStore {

    /** Adds the member's like; false when it was already there. */
    boolean add(UUID memberId, UUID showcaseId, Instant at);

    /** Removes the member's like; false when there was none. */
    boolean remove(UUID memberId, UUID showcaseId);

    int count(UUID showcaseId);

    boolean exists(UUID memberId, UUID showcaseId);
}
