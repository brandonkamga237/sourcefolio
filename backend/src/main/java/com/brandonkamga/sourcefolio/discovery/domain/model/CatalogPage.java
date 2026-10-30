package com.brandonkamga.sourcefolio.discovery.domain.model;

import java.util.List;

/** One page of the catalogue; nextCursor is null on the last page, total counts the whole catalogue. */
public record CatalogPage(List<CatalogEntry> entries, String nextCursor, long total) {

    public CatalogPage {
        entries = List.copyOf(entries);
    }
}
