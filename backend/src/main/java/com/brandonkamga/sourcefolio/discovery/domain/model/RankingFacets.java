package com.brandonkamga.sourcefolio.discovery.domain.model;

import java.util.List;

/** How many listed profiles each country and each language has, most first. */
public record RankingFacets(List<Count> countries, List<Count> languages) {

    public RankingFacets {
        countries = List.copyOf(countries);
        languages = List.copyOf(languages);
    }

    public record Count(String value, long count) {
    }
}
