package com.brandonkamga.sourcefolio.showcase.infra.jobs;

import com.brandonkamga.sourcefolio.showcase.application.RepositoryStars;
import java.util.UUID;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.annotations.Recurring;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.stereotype.Component;

/**
 * GitHub stars of the showcases' repositories. Every hour, one query picks the repositories not
 * read for a day and one job per repository reads it: 40 an hour stays under the 60 calls GitHub
 * allows without a token, shared with detection.
 */
@Component
public class RefreshStarsJob {

    private static final int PER_HOUR = 40;

    private final RepositoryStars stars;
    private final JobScheduler jobs;

    public RefreshStarsJob(RepositoryStars stars, JobScheduler jobs) {
        this.stars = stars;
        this.jobs = jobs;
    }

    @Recurring(id = "repository-stars", cron = "23 * * * *")
    @Job(name = "Pick the repositories whose stars are due", labels = "github")
    public void sweep() {
        stars.listDue(PER_HOUR).forEach(showcaseId -> jobs.<RefreshStarsJob>enqueue(job -> job.refresh(showcaseId)));
    }

    // A rate limit or an outage throws: one retry, and the next sweep picks it up anyway.
    @Job(name = "Read the stars of showcase %0", labels = "github", retries = 1)
    public void refresh(UUID showcaseId) {
        stars.refresh(showcaseId);
    }
}
