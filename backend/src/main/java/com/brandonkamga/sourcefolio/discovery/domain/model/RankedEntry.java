package com.brandonkamga.sourcefolio.discovery.domain.model;

/** A catalogue entry with its public like count, as the Ranking lists it. */
public record RankedEntry(CatalogEntry entry, int likes) {
}
