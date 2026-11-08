package com.brandonkamga.sourcefolio.contribution.infra.jobs;

import com.brandonkamga.sourcefolio.contribution.application.ContributionAnalysis;
import java.util.UUID;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.annotations.Recurring;
import org.springframework.stereotype.Component;

/**
 * Readings of public GitHub data: one job per member, after a sign-in or for the weekly refresh.
 * A reading costs a handful of GraphQL points; 200 an hour stays far below the 5 000 points an
 * hour of the platform's token.
 */
@Component
public class AnalyzeContributionsJob {

    private static final int PER_HOUR = 200;

    private final ContributionAnalysis analysis;

    public AnalyzeContributionsJob(ContributionAnalysis analysis) {
        this.analysis = analysis;
    }

    @Recurring(id = "contribution-weekly", cron = "41 * * * *")
    @Job(name = "Queue the weekly readings that are due", labels = "github")
    public void sweep() {
        analysis.scheduleDue(PER_HOUR);
    }

    // A rate limit or an outage throws: two retries with JobRunr's backoff, then the sweep of the
    // next week picks the member up again.
    @Job(name = "Read the public GitHub data of member %0", labels = "github", retries = 2)
    public void analyze(UUID memberId) {
        analysis.analyze(memberId);
    }
}
