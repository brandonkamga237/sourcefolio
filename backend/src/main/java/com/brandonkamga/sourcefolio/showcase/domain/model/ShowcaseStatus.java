package com.brandonkamga.sourcefolio.showcase.domain.model;

/** Where a showcase is in its lifecycle. Only PUBLISHED shows in the catalogue. */
public enum ShowcaseStatus {
    PUBLISHED,
    /** Three failed checks within 24 hours. Hidden from the catalogue, still reachable by its link. */
    UNAVAILABLE,
    /** Unavailable for 14 days, or its content changed radically: removal in 7 days. */
    NEEDS_FIX,
    /** Invisible to the public; the author can fix the link for 12 months. */
    REMOVED
}
