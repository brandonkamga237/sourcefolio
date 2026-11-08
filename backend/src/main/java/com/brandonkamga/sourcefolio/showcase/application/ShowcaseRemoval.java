package com.brandonkamga.sourcefolio.showcase.application;

import java.util.UUID;

/**
 * All that remains of the retired portfolio catalogue. The foreign keys take a deleted member's
 * rows, but not the preview files they uploaded, which stay readable at a public address until
 * something deletes them. Goes away with the decision on the portfolio tables and bucket.
 */
public interface ShowcaseRemoval {

    void removeAllOf(UUID memberId);
}
