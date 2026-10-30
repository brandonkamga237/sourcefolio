package com.brandonkamga.sourcefolio.showcase.api;

import com.brandonkamga.sourcefolio.showcase.application.ShowcaseDetection;
import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateReason;
import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateScan;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseCandidate;
import com.brandonkamga.sourcefolio.showcase.domain.model.SiteUrl;
import com.brandonkamga.sourcefolio.shared.security.SecurityTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShowcaseCandidatesController.class)
@Import(SecurityTestConfig.class)
class ShowcaseCandidatesControllerTest {

    private static final UUID MEMBER = UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ShowcaseDetection detection;

    @Test
    @DisplayName("before the first detection finishes, scannedAt is null and the list empty: the screen shows 'searching'")
    void notScannedYet() throws Exception {
        when(detection.findCandidates(MEMBER)).thenReturn(Optional.empty());

        mvc.perform(get("/api/me/showcase-candidates").with(member()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scannedAt").value(nullValue()))
                .andExpect(jsonPath("$.candidates").isEmpty());
    }

    @Test
    @DisplayName("the candidates come best first, with their repository, site and reason")
    void candidates() throws Exception {
        when(detection.findCandidates(MEMBER)).thenReturn(Optional.of(new CandidateScan(Instant.parse("2026-10-30T09:00:00Z"), List.of(
                new ShowcaseCandidate(new RepositoryCoordinates("nekwalla", "nekwalla.github.io"),
                        new SiteUrl("https://nekwalla.github.io/"), CandidateReason.USER_PAGES)))));

        mvc.perform(get("/api/me/showcase-candidates").with(member()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scannedAt").value("2026-10-30T09:00:00Z"))
                .andExpect(jsonPath("$.candidates[0].repository").value("nekwalla/nekwalla.github.io"))
                .andExpect(jsonPath("$.candidates[0].repositoryUrl").value("https://github.com/nekwalla/nekwalla.github.io"))
                .andExpect(jsonPath("$.candidates[0].siteUrl").value("https://nekwalla.github.io/"))
                .andExpect(jsonPath("$.candidates[0].reason").value("USER_PAGES"));
    }

    @Test
    @DisplayName("a visitor has no candidates to see")
    void visitorGets401() throws Exception {
        mvc.perform(get("/api/me/showcase-candidates")).andExpect(status().isUnauthorized());
    }

    private static RequestPostProcessor member() {
        return oauth2Login().oauth2User(new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_MEMBER")), Map.of("id", MEMBER.toString()), "id"));
    }
}
