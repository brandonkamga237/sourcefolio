package com.brandonkamga.sourcefolio.discovery.domain.model;

import java.util.List;

/** One page of the catalogue; nextCursor is null on the last page. */
public record CatalogPage(List<CatalogEntry> entries, String nextCursor) {

    public CatalogPage {
        entries = List.copyOf(entries);
    }
}
