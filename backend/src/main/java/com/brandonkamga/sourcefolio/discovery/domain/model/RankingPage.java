package com.brandonkamga.sourcefolio.discovery.domain.model;

import java.util.List;

/** A page of a ranking; nextCursor is null on the last page, total counts every listed row. */
public record RankingPage(List<RankingRow> rows, String nextCursor, long total) {

    public RankingPage {
        rows = List.copyOf(rows);
    }
}
