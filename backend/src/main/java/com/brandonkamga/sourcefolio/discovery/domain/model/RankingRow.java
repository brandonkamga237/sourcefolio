package com.brandonkamga.sourcefolio.discovery.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** One listed profile with its position in the ranking asked for. */
public record RankingRow(int position, UUID memberId, String login, String name, String countryCode, String city,
                         Instant accountCreatedAt, int publicContributions, int activeMonths, List<Integer> months, int stars,
                         int followers, int mergedPullRequestsElsewhere, List<String> languages) {

    public RankingRow {
        months = List.copyOf(months);
        languages = List.copyOf(languages);
    }
}
