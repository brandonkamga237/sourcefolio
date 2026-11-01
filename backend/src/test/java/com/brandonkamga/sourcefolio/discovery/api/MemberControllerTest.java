package com.brandonkamga.sourcefolio.discovery.api;

import com.brandonkamga.sourcefolio.discovery.application.Members;
import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogEntry;
import com.brandonkamga.sourcefolio.discovery.domain.model.MemberCard;
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
import java.util.Optional;
import java.util.UUID;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
@Import({SecurityTestConfig.class, CatalogRoutesTestConfig.class})
class MemberControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private Members members;

    private static final CatalogEntry SHOWCASE = new CatalogEntry(UUID.randomUUID(), "nekwalla", "https://nekwalla.github.io/",
            "nekwalla/nekwalla.github.io", "https://github.com/nekwalla/nekwalla.github.io", UUID.randomUUID(), "Nekwalla",
            Instant.parse("2026-10-30T09:00:00Z"), "https://media.test/p.webp", null, 12, 4, "CM", List.of("backend"));

    @Test
    @DisplayName("a visitor sees a published member: profile, first day, likes received, portfolio card, and no ids")
    void member() throws Exception {
        when(members.findByLogin("nekwalla")).thenReturn(Optional.of(
                new MemberCard("Nekwalla", "CM", List.of("backend", "devops"), "Backend à Douala", "devops", Instant.parse("2026-09-01T09:00:00Z"), 7, SHOWCASE)));

        mvc.perform(get("/api/members/nekwalla"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("Nekwalla"))
                .andExpect(jsonPath("$.githubUrl").value("https://github.com/Nekwalla"))
                .andExpect(jsonPath("$.countryCode").value("CM"))
                .andExpect(jsonPath("$.specialties[1]").value("devops"))
                .andExpect(jsonPath("$.headline").value("Backend à Douala"))
                .andExpect(jsonPath("$.role").value("devops"))
                .andExpect(jsonPath("$.onSourceFolioSince").value("2026-09-01T09:00:00Z"))
                .andExpect(jsonPath("$.likesReceived").value(7))
                .andExpect(jsonPath("$.showcase.slug").value("nekwalla"))
                .andExpect(jsonPath("$.showcase.repositoryStars").value(12))
                .andExpect(jsonPath("$.showcase.showcaseId").doesNotExist())
                .andExpect(jsonPath("$.ownerId").doesNotExist());
    }

    @Test
    @DisplayName("a login without a published portfolio is a 404 in the visitor's language")
    void unknown() throws Exception {
        when(members.findByLogin("ghost")).thenReturn(Optional.empty());

        mvc.perform(get("/api/members/ghost").header("Accept-Language", "fr"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ce membre n’a pas de portfolio publié."));
    }
}
