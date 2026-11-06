package com.brandonkamga.sourcefolio.contribution.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * A member's analysis: the login GitHub knows them by, when a reading was last asked for, and
 * the latest snapshot. Logins change, so the member id is the identity and the login follows.
 */
public record ContributionRecord(UUID memberId, String login, Instant requestedAt, Instant failedAt,
                                 ContributionSnapshot snapshot) {

    // At most one reading an hour from sign-ins; a pending one is not asked twice while the job
    // is likely still queued.
    static final Duration FRESH_FOR = Duration.ofHours(1);
    static final Duration PENDING_FOR = Duration.ofMinutes(2);

    public static ContributionRecord requested(UUID memberId, String login, Instant now) {
        return new ContributionRecord(memberId, login, now, null, null);
    }

    public Optional<ContributionSnapshot> latest() {
        return Optional.ofNullable(snapshot);
    }

    public AnalysisStatus status() {
        if (snapshot != null) {
            return AnalysisStatus.READY;
        }
        return failedAt != null && !failedAt.isBefore(requestedAt) ? AnalysisStatus.FAILED : AnalysisStatus.PENDING;
    }

    /** True while a reading asked for has not produced anything yet. */
    public boolean refreshing() {
        boolean answered = (snapshot != null && !snapshot.analyzedAt().isBefore(requestedAt))
                || (failedAt != null && !failedAt.isBefore(requestedAt));
        return !answered;
    }

    public boolean worthReading(Instant now) {
        if (snapshot != null && snapshot.analyzedAt().isAfter(now.minus(FRESH_FOR))) {
            return false;
        }
        return !(refreshing() && requestedAt.isAfter(now.minus(PENDING_FOR)));
    }

    public ContributionRecord askedAgain(String currentLogin, Instant now) {
        return new ContributionRecord(memberId, currentLogin, now, failedAt, snapshot);
    }

    public ContributionRecord renamed(String currentLogin) {
        return new ContributionRecord(memberId, currentLogin, requestedAt, failedAt, snapshot);
    }

    public ContributionRecord analyzed(ContributionSnapshot fresh) {
        return new ContributionRecord(memberId, login, requestedAt, failedAt, fresh);
    }

    // An earlier snapshot is kept: a login GitHub briefly cannot resolve must not wipe a profile.
    public ContributionRecord failed(Instant now) {
        return new ContributionRecord(memberId, login, requestedAt, now, snapshot);
    }
}
