package com.brandonkamga.sourcefolio.discovery.domain.model;

import java.util.List;

/** The countries, source languages and declared roles present in the catalogue, most frequent first: what the filters offer. */
public record CatalogFacets(List<Facet> countries, List<Facet> languages, List<Facet> roles) {

    public CatalogFacets {
        countries = List.copyOf(countries);
        languages = List.copyOf(languages);
        roles = List.copyOf(roles);
    }

    /** A country code, a language name or a role slug, and how many portfolios carry it. */
    public record Facet(String value, long count) {
    }
}
