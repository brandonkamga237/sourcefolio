package com.brandonkamga.sourcefolio.discovery.api;

import com.brandonkamga.sourcefolio.discovery.api.dto.CityCountResponse;
import com.brandonkamga.sourcefolio.discovery.api.dto.ProjectCountResponse;
import com.brandonkamga.sourcefolio.discovery.api.dto.RankingFacetsResponse;
import com.brandonkamga.sourcefolio.discovery.api.dto.RankingsResponse;
import com.brandonkamga.sourcefolio.discovery.application.Rankings;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class RankingsController {

    private final Rankings rankings;

    RankingsController(Rankings rankings) {
        this.rankings = rankings;
    }

    /** sort: regularity (default), contributions, stars, external, seniority. */
    @GetMapping("/api/rankings")
    @Operation(operationId = "listRankings")
    RankingsResponse list(@RequestParam(required = false) String sort, @RequestParam(required = false) String country,
                          @RequestParam(required = false) String language, @RequestParam(required = false) String cursor,
                          @RequestParam(defaultValue = "50") int size) {
        return RankingsResponse.of(rankings.listRanking(sort, country, language, cursor, size));
    }

    @GetMapping("/api/rankings/facets")
    @Operation(operationId = "listRankingFacets")
    RankingFacetsResponse facets(@RequestParam(required = false) String country, @RequestParam(required = false) String language) {
        return RankingFacetsResponse.of(rankings.listFacets(country, language));
    }

    @GetMapping("/api/rankings/projects")
    @Operation(operationId = "listAcceptedProjects")
    List<ProjectCountResponse> projects() {
        return rankings.listAcceptedProjects().stream().map(ProjectCountResponse::of).toList();
    }

    @GetMapping("/api/rankings/cities")
    @Operation(operationId = "listRankingCities")
    List<CityCountResponse> cities() {
        return rankings.listCityCounts().stream().map(CityCountResponse::of).toList();
    }
}
