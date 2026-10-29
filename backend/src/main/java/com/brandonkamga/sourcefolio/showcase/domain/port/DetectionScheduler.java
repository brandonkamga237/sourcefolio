package com.brandonkamga.sourcefolio.showcase.domain.port;

import java.util.UUID;

/** Runs detection in the background, so signing in never waits on GitHub. */
public interface DetectionScheduler {

    void scheduleDetection(UUID memberId, String login);
}
