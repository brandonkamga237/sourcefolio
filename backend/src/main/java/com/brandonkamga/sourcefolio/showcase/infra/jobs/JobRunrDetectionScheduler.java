package com.brandonkamga.sourcefolio.showcase.infra.jobs;

import com.brandonkamga.sourcefolio.showcase.domain.port.DetectionScheduler;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class JobRunrDetectionScheduler implements DetectionScheduler {

    private final JobScheduler jobs;

    JobRunrDetectionScheduler(JobScheduler jobs) {
        this.jobs = jobs;
    }

    // The IoC form stores the job's class and arguments, not a captured bean: JobRunr asks Spring
    // for DetectCandidatesJob when the job runs, possibly after a redeployment.
    @Override
    public void scheduleDetection(UUID memberId, String login) {
        jobs.<DetectCandidatesJob>enqueue(job -> job.run(memberId, login));
    }
}
