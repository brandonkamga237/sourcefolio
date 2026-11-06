package com.brandonkamga.sourcefolio.contribution.application;

import java.util.UUID;

/**
 * Reading a member's public GitHub data. Not wrapped in a transaction: analyze calls GitHub, and
 * a network call never holds one; the store opens its own around each write.
 */
public interface ContributionAnalysis {

    /** Asks for a reading after a sign-in; does nothing if one is fresh or already queued. */
    void requestAnalysis(UUID memberId, String login);

    /** The job's work: one reading, idempotent. GitHub outages throw so the job retries. */
    void analyze(UUID memberId);

    /** Queues the weekly reading of the members most overdue; returns how many were queued. */
    int scheduleDue(int limit);
}
