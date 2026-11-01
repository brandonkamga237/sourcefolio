package com.brandonkamga.sourcefolio.discovery.domain.model;

import java.util.List;

/** The countries and source languages present in the catalogue, most frequent first: what the filters offer. */
public record CatalogFacets(List<Facet> countries, List<Facet> languages) {

    public CatalogFacets {
        countries = List.copyOf(countries);
        languages = List.copyOf(languages);
    }

    /** A country code or a language name, and how many portfolios carry it. */
    public record Facet(String value, long count) {
    }
}
