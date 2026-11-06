package com.brandonkamga.sourcefolio.discovery.api.dto;

import com.brandonkamga.sourcefolio.discovery.domain.model.RankingFacets;
import java.util.List;

/** Listed profiles per country (ISO code) and per language, most first. */
public record RankingFacetsResponse(List<Count> countries, List<Count> languages) {

    public static RankingFacetsResponse of(RankingFacets facets) {
        return new RankingFacetsResponse(
                facets.countries().stream().map(count -> new Count(count.value(), count.count())).toList(),
                facets.languages().stream().map(count -> new Count(count.value(), count.count())).toList());
    }

    public record Count(String value, long count) {
    }
}
