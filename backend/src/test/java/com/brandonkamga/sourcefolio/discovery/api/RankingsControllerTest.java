package com.brandonkamga.sourcefolio.discovery.api;

import com.brandonkamga.sourcefolio.discovery.application.Rankings;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingFacets;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingPage;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingRow;
import com.brandonkamga.sourcefolio.discovery.infra.security.CatalogRoutesTestConfig;
import com.brandonkamga.sourcefolio.shared.exception.BadRequestException;
import com.brandonkamga.sourcefolio.shared.security.SecurityTestConfig;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RankingsController.class)
@Import({SecurityTestConfig.class, CatalogRoutesTestConfig.class})
class RankingsControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private Rankings rankings;

    @Test
    @DisplayName("a visitor reads a ranking: position, indicators, regularity strip, country code")
    void ranking() throws Exception {
        when(rankings.listRanking("stars", "CM", null, null, 50)).thenReturn(new RankingPage(List.of(
                new RankingRow(1, UUID.randomUUID(), "nkeng-dev", "Nkeng Tabi", "CM", "Yaoundé", Instant.parse("2017-03-14T08:00:00Z"),
                        2140, 12, List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), 512, 94, 14, List.of("PHP", "TypeScript"))),
                "next-page", 7));

        mvc.perform(get("/api/rankings").param("sort", "stars").param("country", "CM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(7))
                .andExpect(jsonPath("$.nextCursor").value("next-page"))
                .andExpect(jsonPath("$.items[0].position").value(1))
                .andExpect(jsonPath("$.items[0].login").value("nkeng-dev"))
                .andExpect(jsonPath("$.items[0].country").value("CM"))
                .andExpect(jsonPath("$.items[0].months.length()").value(12))
                .andExpect(jsonPath("$.items[0].languages[0]").value("PHP"));
    }

    @Test
    @DisplayName("facets are public too")
    void facets() throws Exception {
        when(rankings.listFacets(null, "Go")).thenReturn(new RankingFacets(List.of(new RankingFacets.Count("NG", 4)), List.of()));

        mvc.perform(get("/api/rankings/facets").param("language", "Go"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countries[0].value").value("NG"))
                .andExpect(jsonPath("$.countries[0].count").value(4));
    }

    @Test
    @DisplayName("an unknown sort is a 400 in the visitor's language")
    void unknownSort() throws Exception {
        when(rankings.listRanking("likes", null, null, null, 50)).thenThrow(new BadRequestException("ranking.sort-invalid"));

        mvc.perform(get("/api/rankings").param("sort", "likes").header("Accept-Language", "fr"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Choisis un classement de la liste."));
    }
}
