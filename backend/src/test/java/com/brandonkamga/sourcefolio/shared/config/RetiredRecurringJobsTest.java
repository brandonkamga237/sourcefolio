package com.brandonkamga.sourcefolio.shared.config;

import org.jobrunr.scheduling.JobScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RetiredRecurringJobsTest {

    @Test
    @DisplayName("the job refreshing portfolio stars is unregistered at startup")
    void unregistersTheJob() {
        JobScheduler jobs = mock(JobScheduler.class);

        new RetiredRecurringJobs(jobs).run(null);

        verify(jobs).deleteRecurringJob("repository-stars");
    }
}
