package com.brandonkamga.sourcefolio.showcase.infra.jobs;

import com.brandonkamga.sourcefolio.showcase.application.RepositoryRefresh;
import java.util.UUID;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.annotations.Recurring;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.stereotype.Component;

/**
 * What GitHub says about the showcases' repositories (stars, description, languages, last push).
 * Every hour, one query picks the repositories not read for a day and one job per repository
 * reads it, in two calls: 25 an hour stays under the 60 calls GitHub allows without a token,
 * shared with detection.
 */
@Component
public class RefreshRepositoryJob {

    private static final int PER_HOUR = 25;

    private final RepositoryRefresh stars;
    private final JobScheduler jobs;

    public RefreshRepositoryJob(RepositoryRefresh stars, JobScheduler jobs) {
        this.stars = stars;
        this.jobs = jobs;
    }

    @Recurring(id = "repository-stars", cron = "23 * * * *")
    @Job(name = "Pick the repositories due for a read", labels = "github")
    public void sweep() {
        stars.listDue(PER_HOUR).forEach(showcaseId -> jobs.<RefreshRepositoryJob>enqueue(job -> job.refresh(showcaseId)));
    }

    // A rate limit or an outage throws: one retry, and the next sweep picks it up anyway.
    @Job(name = "Read the repository of showcase %0", labels = "github", retries = 1)
    public void refresh(UUID showcaseId) {
        stars.refresh(showcaseId);
    }
}
