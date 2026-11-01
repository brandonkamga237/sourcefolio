package com.brandonkamga.sourcefolio.discovery.api;

import com.brandonkamga.sourcefolio.discovery.api.dto.CatalogResponse;
import com.brandonkamga.sourcefolio.discovery.api.dto.FacetsResponse;
import com.brandonkamga.sourcefolio.discovery.api.dto.RankingResponse;
import com.brandonkamga.sourcefolio.discovery.application.Catalog;
import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogFilter;
import com.brandonkamga.sourcefolio.shared.exception.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class CatalogController {

    private final Catalog catalog;

    CatalogController(Catalog catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/api/catalog")
    @Operation(operationId = "getCatalog")
    CatalogResponse catalog(@RequestParam(defaultValue = "discover") String sort,
                            @RequestParam(required = false) String cursor,
                            @RequestParam(defaultValue = "20") int size,
                            @RequestParam(required = false) String q,
                            @RequestParam(required = false) String country,
                            @RequestParam(required = false) String specialty) {
        CatalogFilter filter = CatalogFilter.parse(q, country, specialty).orElseThrow(() -> new BadRequestException("catalog.filter-invalid"));
        return CatalogResponse.of(switch (sort) {
            case "discover" -> catalog.listDiscover(cursor, size, filter);
            case "recent" -> catalog.listRecent(cursor, size, filter);
            default -> throw new BadRequestException("catalog.sort-invalid");
        });
    }

    @GetMapping("/api/catalog/facets")
    @Operation(operationId = "getCatalogFacets")
    FacetsResponse facets() {
        return FacetsResponse.of(catalog.findFacets());
    }

    @GetMapping("/api/ranking")
    @Operation(operationId = "getRanking")
    RankingResponse ranking(@RequestParam(defaultValue = "100") int size) {
        return RankingResponse.of(catalog.listRanking(size));
    }
}
