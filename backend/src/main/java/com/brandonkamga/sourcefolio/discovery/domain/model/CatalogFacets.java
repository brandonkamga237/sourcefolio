package com.brandonkamga.sourcefolio.discovery.domain.model;

import java.util.List;

/** The countries and interests present in the catalogue, most frequent first: what the filters offer. */
public record CatalogFacets(List<Facet> countries, List<Facet> specialties) {

    public CatalogFacets {
        countries = List.copyOf(countries);
        specialties = List.copyOf(specialties);
    }

    /** A country code or an interest slug, and how many portfolios carry it. */
    public record Facet(String value, long count) {
    }
}
