package com.brandonkamga.sourcefolio.discovery.api.dto;

import com.brandonkamga.sourcefolio.discovery.domain.model.RankingPage;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingRow;
import java.time.Instant;
import java.util.List;

/**
 * A page of a ranking. position is the rank in the whole ranking asked for; months the
 * contributions of the last twelve months, oldest first; country an ISO code.
 */
public record RankingsResponse(List<Row> items, String nextCursor, long total) {

    public static RankingsResponse of(RankingPage page) {
        return new RankingsResponse(page.rows().stream().map(Row::of).toList(), page.nextCursor(), page.total());
    }

    public record Row(int position, String login, String name, String country, String city, Instant accountCreatedAt,
                      int publicContributions, int activeMonths, List<Integer> months, int stars, int followers,
                      int mergedPullRequestsElsewhere, List<String> languages) {

        static Row of(RankingRow row) {
            return new Row(row.position(), row.login(), row.name(), row.countryCode(), row.city(), row.accountCreatedAt(),
                    row.publicContributions(), row.activeMonths(), row.months(), row.stars(), row.followers(),
                    row.mergedPullRequestsElsewhere(), row.languages());
        }
    }
}
