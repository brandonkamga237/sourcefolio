package com.brandonkamga.sourcefolio.contribution.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A member's public GitHub data was read (or their login changed): the indicators the rankings
 * sort on. months holds the contributions of the last twelve months, oldest first; languages
 * the largest first.
 */
public record ContributionAnalyzed(UUID memberId, String login, String name, Instant analyzedAt, Instant accountCreatedAt,
                                   int publicContributions, int activeMonths, List<Integer> months, int stars, int followers,
                                   int mergedPullRequestsElsewhere, List<String> languages) {

    public ContributionAnalyzed {
        months = List.copyOf(months);
        languages = List.copyOf(languages);
    }
}
