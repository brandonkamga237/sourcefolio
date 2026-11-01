package com.brandonkamga.sourcefolio.discovery.api.dto;

import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogFacets;
import java.util.List;

/** Country codes, which the frontend translates, and GitHub language names, with their counts. */
public record FacetsResponse(List<Facet> countries, List<Facet> languages) {

    public record Facet(String value, long count) {

        static Facet of(CatalogFacets.Facet facet) {
            return new Facet(facet.value(), facet.count());
        }
    }

    public static FacetsResponse of(CatalogFacets facets) {
        return new FacetsResponse(facets.countries().stream().map(Facet::of).toList(), facets.languages().stream().map(Facet::of).toList());
    }
}
