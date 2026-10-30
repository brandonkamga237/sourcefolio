package com.brandonkamga.sourcefolio.showcase.domain.model;

import java.time.Instant;
import java.util.Objects;

/** One status change. {@code from} is null for the first status a showcase gets. */
public record ShowcaseStatusEvent(ShowcaseStatus from, ShowcaseStatus to, String reason, StatusActor actor, Instant at) {

    /** The site answered and the repository and its homepage belong to the member. */
    public static final String AUTO_VERIFIED = "AUTO_VERIFIED";

    public ShowcaseStatusEvent {
        Objects.requireNonNull(to, "to");
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(actor, "actor");
        Objects.requireNonNull(at, "at");
    }
}
