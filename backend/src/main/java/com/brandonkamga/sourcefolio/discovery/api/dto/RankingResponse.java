package com.brandonkamga.sourcefolio.discovery.api.dto;

import com.brandonkamga.sourcefolio.discovery.domain.model.RankedEntry;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

/** The Ranking tab: rank 1 is the most liked. */
public record RankingResponse(List<Row> items) {

    public record Row(int rank, String slug, String siteUrl, String repository, String repositoryUrl, String ownerLogin,
                      Instant publishedAt, int likes) {
    }

    public static RankingResponse of(List<RankedEntry> entries) {
        return new RankingResponse(IntStream.range(0, entries.size()).mapToObj(index -> {
            RankedEntry ranked = entries.get(index);
            var entry = ranked.entry();
            return new Row(index + 1, entry.slug(), entry.siteUrl(), entry.repository(), entry.repositoryUrl(), entry.ownerLogin(),
                    entry.publishedAt(), ranked.likes());
        }).toList());
    }
}
