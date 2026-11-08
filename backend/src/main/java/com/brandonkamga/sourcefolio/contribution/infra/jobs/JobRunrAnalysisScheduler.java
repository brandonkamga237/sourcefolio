package com.brandonkamga.sourcefolio.contribution.infra.jobs;

import com.brandonkamga.sourcefolio.contribution.domain.port.AnalysisScheduler;
import java.util.UUID;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.stereotype.Component;

@Component
class JobRunrAnalysisScheduler implements AnalysisScheduler {

    private final JobScheduler jobs;

    JobRunrAnalysisScheduler(JobScheduler jobs) {
        this.jobs = jobs;
    }

    // The IoC form stores the job's class and arguments, so the job survives a redeployment.
    @Override
    public void scheduleAnalysis(UUID memberId) {
        jobs.<AnalyzeContributionsJob>enqueue(job -> job.analyze(memberId));
    }
}
