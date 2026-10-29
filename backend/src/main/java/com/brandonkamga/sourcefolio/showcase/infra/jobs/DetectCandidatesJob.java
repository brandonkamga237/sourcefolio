package com.brandonkamga.sourcefolio.showcase.infra.jobs;

import com.brandonkamga.sourcefolio.showcase.application.ShowcaseDetection;
import org.jobrunr.jobs.annotations.Job;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * The background job of portfolio detection: ids in, one use case call out. A GitHub outage or
 * rate limit throws, and JobRunr retries with backoff; the use case is idempotent.
 */
@Component
public class DetectCandidatesJob {

    private final ShowcaseDetection detection;

    public DetectCandidatesJob(ShowcaseDetection detection) {
        this.detection = detection;
    }

    @Job(name = "Detect the showcase candidates of %1", labels = "github", retries = 3)
    public void run(UUID memberId, String login) {
        detection.detect(memberId, login);
    }
}
