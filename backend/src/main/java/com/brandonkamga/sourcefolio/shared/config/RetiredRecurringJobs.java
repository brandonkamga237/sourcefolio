package com.brandonkamga.sourcefolio.shared.config;

import org.jobrunr.scheduling.JobScheduler;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * JobRunr keeps a recurring job in its own tables after the class is deleted, and would go on
 * failing it every hour. Safe to remove once every environment has started with this version.
 */
@Component
class RetiredRecurringJobs implements ApplicationRunner {

    private final JobScheduler jobs;

    RetiredRecurringJobs(JobScheduler jobs) {
        this.jobs = jobs;
    }

    @Override
    public void run(ApplicationArguments args) {
        jobs.deleteRecurringJob("repository-stars");
    }
}
