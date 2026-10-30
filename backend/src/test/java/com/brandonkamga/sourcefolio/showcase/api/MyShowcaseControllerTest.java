package com.brandonkamga.sourcefolio.showcase.api;

import com.brandonkamga.sourcefolio.shared.exception.ConflictException;
import com.brandonkamga.sourcefolio.shared.security.SecurityTestConfig;
import com.brandonkamga.sourcefolio.showcase.application.PublishShowcase;
import com.brandonkamga.sourcefolio.showcase.application.ShowcasePublication;
import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateReason;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseCandidate;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseSlug;
import com.brandonkamga.sourcefolio.showcase.domain.model.SiteUrl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MyShowcaseController.class)
@Import(SecurityTestConfig.class)
class MyShowcaseControllerTest {

    private static final UUID MEMBER = UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11");
    private static final Instant NOW = Instant.parse("2026-10-30T09:00:00Z");
    private static final Showcase PUBLISHED = Showcase.publishVerified(MEMBER, new ShowcaseSlug("nekwalla"), new ShowcaseCandidate(
            new RepositoryCoordinates("nekwalla", "nekwalla.github.io"), new SiteUrl("https://nekwalla.github.io/"),
            CandidateReason.USER_PAGES), NOW).stored();

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ShowcasePublication publication;

    @Test
    @DisplayName("before the first publication, showcase is null")
    void noShowcaseYet() throws Exception {
        when(publication.findMyShowcase(MEMBER)).thenReturn(Optional.empty());

        mvc.perform(get("/api/me/showcase").with(member()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.showcase").value(nullValue()));
    }

    @Test
    @DisplayName("the member's showcase comes with its site, repository and status")
    void myShowcase() throws Exception {
        when(publication.findMyShowcase(MEMBER)).thenReturn(Optional.of(PUBLISHED));

        mvc.perform(get("/api/me/showcase").with(member()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.showcase.id").value(PUBLISHED.id().toString()))
                .andExpect(jsonPath("$.showcase.slug").value("nekwalla"))
                .andExpect(jsonPath("$.showcase.siteUrl").value("https://nekwalla.github.io/"))
                .andExpect(jsonPath("$.showcase.repository").value("nekwalla/nekwalla.github.io"))
                .andExpect(jsonPath("$.showcase.repositoryUrl").value("https://github.com/nekwalla/nekwalla.github.io"))
                .andExpect(jsonPath("$.showcase.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.showcase.publishedAt").value("2026-10-30T09:00:00Z"));
    }

    @Test
    @DisplayName("publishing a candidate answers 201 with the published showcase")
    void publish() throws Exception {
        when(publication.publish(new PublishShowcase(MEMBER, new RepositoryCoordinates("nekwalla", "nekwalla.github.io"))))
                .thenReturn(PUBLISHED);

        mvc.perform(post("/api/me/showcase").with(member()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"repository\":\"nekwalla/nekwalla.github.io\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.showcase.status").value("PUBLISHED"));
    }

    @Test
    @DisplayName("a site that does not answer gives a 409 with the reason in the member's language")
    void siteUnreachable() throws Exception {
        when(publication.publish(any())).thenThrow(new ConflictException("showcase.site-unreachable", "https://nekwalla.github.io/"));

        mvc.perform(post("/api/me/showcase").with(member()).with(csrf()).header("Accept-Language", "fr")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"repository\":\"nekwalla/nekwalla.github.io\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Ton site https://nekwalla.github.io/ n’a pas répondu. Vérifie qu’il est en ligne, puis réessaie."));
    }

    @Test
    @DisplayName("a repository that is not owner/name is a 400, and nothing is attempted")
    void invalidRepository() throws Exception {
        for (String body : List.of("{\"repository\":\"portfolio\"}", "{\"repository\":\"\"}", "{}")) {
            mvc.perform(post("/api/me/showcase").with(member()).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(publication);
    }

    @Test
    @DisplayName("publishing without the CSRF token is refused")
    void csrfRequired() throws Exception {
        mvc.perform(post("/api/me/showcase").with(member())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"repository\":\"nekwalla/portfolio\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(publication);
    }

    @Test
    @DisplayName("a visitor can neither see nor publish a showcase")
    void visitorGets401() throws Exception {
        mvc.perform(get("/api/me/showcase")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/me/showcase").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"repository\":\"nekwalla/portfolio\"}"))
                .andExpect(status().isUnauthorized());
    }

    private static RequestPostProcessor member() {
        return oauth2Login().oauth2User(new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_MEMBER")), Map.of("id", MEMBER.toString()), "id"));
    }
}
