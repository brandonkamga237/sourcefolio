package com.brandonkamga.sourcefolio.discovery.api;

import com.brandonkamga.sourcefolio.discovery.application.Catalog;
import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogEntry;
import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogFilter;
import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogPage;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankedEntry;
import com.brandonkamga.sourcefolio.discovery.infra.security.CatalogRoutesTestConfig;
import com.brandonkamga.sourcefolio.shared.security.SecurityTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CatalogController.class)
@Import({SecurityTestConfig.class, CatalogRoutesTestConfig.class})
class CatalogControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private Catalog catalog;

    private static final CatalogEntry ENTRY = new CatalogEntry(UUID.randomUUID(), "nekwalla", "https://nekwalla.github.io/",
            "nekwalla/nekwalla.github.io", "https://github.com/nekwalla/nekwalla.github.io", UUID.randomUUID(), "Nekwalla",
            Instant.parse("2026-10-30T09:00:00Z"), null, null, 12, 4, "CM", List.of("backend", "devops"), "Backend à Douala");

    @Test
    @DisplayName("search words, country and interest reach the use case, tidied")
    void filters() throws Exception {
        CatalogFilter filter = new CatalogFilter("react", "CM", "frontend");
        when(catalog.listRecent(null, 20, filter)).thenReturn(new CatalogPage(List.of(ENTRY), null, 1));

        mvc.perform(get("/api/catalog").param("sort", "recent").param("q", " React ").param("country", "cm").param("specialty", "frontend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    @DisplayName("an invalid filter is a 400 in the visitor's language, before the use case")
    void invalidFilter() throws Exception {
        mvc.perform(get("/api/catalog").param("country", "Cameroun").header("Accept-Language", "fr"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
        verifyNoInteractions(catalog);
    }

    @Test
    @DisplayName("a visitor gets the Discover feed by default, without an account, and without ids")
    void discoverByDefault() throws Exception {
        when(catalog.listDiscover(null, 20, CatalogFilter.none())).thenReturn(new CatalogPage(List.of(ENTRY), "next", 124));

        mvc.perform(get("/api/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].slug").value("nekwalla"))
                .andExpect(jsonPath("$.items[0].siteUrl").value("https://nekwalla.github.io/"))
                .andExpect(jsonPath("$.items[0].ownerLogin").value("Nekwalla"))
                .andExpect(jsonPath("$.items[0].publishedAt").value("2026-10-30T09:00:00Z"))
                .andExpect(jsonPath("$.items[0].likes").value(4))
                .andExpect(jsonPath("$.items[0].repositoryStars").value(12))
                .andExpect(jsonPath("$.items[0].countryCode").value("CM"))
                .andExpect(jsonPath("$.items[0].specialties[0]").value("backend"))
                .andExpect(jsonPath("$.items[0].headline").value("Backend à Douala"))
                .andExpect(jsonPath("$.items[0].showcaseId").doesNotExist())
                .andExpect(jsonPath("$.items[0].ownerId").doesNotExist())
                .andExpect(jsonPath("$.nextCursor").value("next"))
                .andExpect(jsonPath("$.total").value(124));
    }

    @Test
    @DisplayName("Recent takes the cursor and size it is given")
    void recent() throws Exception {
        when(catalog.listRecent("abc", 10, CatalogFilter.none())).thenReturn(new CatalogPage(List.of(), null, 0));

        mvc.perform(get("/api/catalog").param("sort", "recent").param("cursor", "abc").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.nextCursor").value(nullValue()));
    }

    @Test
    @DisplayName("a visitor gets the Ranking with ranks and public like counts")
    void ranking() throws Exception {
        when(catalog.listRanking(100)).thenReturn(List.of(new RankedEntry(ENTRY, 42)));

        mvc.perform(get("/api/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].rank").value(1))
                .andExpect(jsonPath("$.items[0].slug").value("nekwalla"))
                .andExpect(jsonPath("$.items[0].likes").value(42));
    }

    @Test
    @DisplayName("Discover has no popularity sort: any other sort is a 400")
    void noPopularitySort() throws Exception {
        mvc.perform(get("/api/catalog").param("sort", "popular")).andExpect(status().isBadRequest());
        verifyNoInteractions(catalog);
    }
}
