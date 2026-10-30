package com.brandonkamga.sourcefolio.showcase.api;

import com.brandonkamga.sourcefolio.shared.security.SecurityTestConfig;
import com.brandonkamga.sourcefolio.showcase.application.PublicShowcase;
import com.brandonkamga.sourcefolio.showcase.application.PublicShowcases;
import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateReason;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseCandidate;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseSlug;
import com.brandonkamga.sourcefolio.showcase.domain.model.SiteUrl;
import com.brandonkamga.sourcefolio.showcase.infra.security.PublicShowcaseRoutesTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicShowcaseController.class)
@Import({SecurityTestConfig.class, PublicShowcaseRoutesTestConfig.class})
class PublicShowcaseControllerTest {

    private static final UUID MEMBER = UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PublicShowcases showcases;

    @Test
    @DisplayName("a visitor opens a showcase from its address, without an account and without the owner's id")
    void visitorSeesShowcase() throws Exception {
        Showcase showcase = Showcase.publishVerified(MEMBER, new ShowcaseSlug("nekwalla"), new ShowcaseCandidate(
                new RepositoryCoordinates("nekwalla", "nekwalla.github.io"), new SiteUrl("https://nekwalla.github.io/"),
                CandidateReason.USER_PAGES), Instant.parse("2026-10-30T09:00:00Z")).stored();
        when(showcases.findBySlug("nekwalla")).thenReturn(Optional.of(new PublicShowcase(showcase, "Nekwalla")));

        mvc.perform(get("/api/showcases/nekwalla"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("nekwalla"))
                .andExpect(jsonPath("$.siteUrl").value("https://nekwalla.github.io/"))
                .andExpect(jsonPath("$.repositoryUrl").value("https://github.com/nekwalla/nekwalla.github.io"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.ownerLogin").value("Nekwalla"))
                .andExpect(jsonPath("$.ownerUrl").value("https://github.com/Nekwalla"))
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.ownerId").doesNotExist());
    }

    @Test
    @DisplayName("an unknown or removed showcase is a 404 in the visitor's language")
    void notFound() throws Exception {
        when(showcases.findBySlug("nobody")).thenReturn(Optional.empty());

        mvc.perform(get("/api/showcases/nobody").header("Accept-Language", "fr"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ce portfolio n’existe pas ou a été retiré."));
    }

    @Test
    @DisplayName("only reading is open: writing to the public route still needs a session")
    void onlyGetIsPublic() throws Exception {
        mvc.perform(post("/api/showcases/nekwalla").with(csrf())).andExpect(status().isUnauthorized());
    }
}
