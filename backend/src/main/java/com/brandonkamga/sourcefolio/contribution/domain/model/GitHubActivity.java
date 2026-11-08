package com.brandonkamga.sourcefolio.contribution.domain.model;

import java.time.Instant;
import java.util.List;

/**
 * What GitHub publicly says about a member at one moment, before any indicator is computed.
 * restrictedContributions are the private ones GitHub counts when the member chose to show them.
 */
public record GitHubActivity(Instant accountCreatedAt, String name, String bio, String location, int followers,
                             int totalContributions, int restrictedContributions, List<CalendarDay> calendar,
                             List<OwnedRepository> repositories, int mergedPullRequestsElsewhere,
                             List<ExternalProject> externalProjects) {

    public GitHubActivity {
        calendar = List.copyOf(calendar);
        repositories = List.copyOf(repositories);
        externalProjects = List.copyOf(externalProjects);
    }
}
