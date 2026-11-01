package com.brandonkamga.sourcefolio.discovery.api.dto;

import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogFacets;
import java.util.List;

/** Country codes and interest slugs with their counts; the frontend translates their names. */
public record FacetsResponse(List<Facet> countries, List<Facet> specialties) {

    public record Facet(String value, long count) {

        static Facet of(CatalogFacets.Facet facet) {
            return new Facet(facet.value(), facet.count());
        }
    }

    public static FacetsResponse of(CatalogFacets facets) {
        return new FacetsResponse(facets.countries().stream().map(Facet::of).toList(), facets.specialties().stream().map(Facet::of).toList());
    }
}
