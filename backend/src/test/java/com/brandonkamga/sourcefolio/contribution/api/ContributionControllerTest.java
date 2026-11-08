package com.brandonkamga.sourcefolio.contribution.api;

import com.brandonkamga.sourcefolio.contribution.application.ContributionAnalysis;
import com.brandonkamga.sourcefolio.contribution.application.ContributionReports;
import com.brandonkamga.sourcefolio.contribution.application.MyAnalysis;
import com.brandonkamga.sourcefolio.contribution.application.ProfileCard;
import com.brandonkamga.sourcefolio.contribution.application.PublicProfiles;
import com.brandonkamga.sourcefolio.profile.application.PublicProfile;
import com.brandonkamga.sourcefolio.contribution.domain.model.AnalysisStatus;
import com.brandonkamga.sourcefolio.contribution.domain.model.CalendarDay;
import com.brandonkamga.sourcefolio.contribution.domain.model.ContributionSnapshot;
import com.brandonkamga.sourcefolio.contribution.domain.model.ExternalProject;
import com.brandonkamga.sourcefolio.contribution.domain.model.GitHubActivity;
import com.brandonkamga.sourcefolio.contribution.infra.security.ProfileRoutesTestConfig;
import com.brandonkamga.sourcefolio.shared.security.SecurityTestConfig;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContributionController.class)
@Import({SecurityTestConfig.class, ProfileRoutesTestConfig.class})
class ContributionControllerTest {

    private static final UUID MEMBER = UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11");
    private static final Instant NOW = Instant.parse("2026-11-06T10:00:00Z");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ContributionReports reports;

    @MockitoBean
    private ContributionAnalysis analysis;

    @MockitoBean
    private PublicProfiles profiles;

    @Test
    @DisplayName("anyone reads a published profile: the member's words and every figure with its proof")
    void publicProfile() throws Exception {
        ContributionSnapshot snapshot = ContributionSnapshot.of(new GitHubActivity(Instant.parse("2017-03-14T08:00:00Z"), "Nkeng Tabi",
                null, null, 94, 2_140, 0, List.of(), List.of(), 14, List.of()), NOW);
        when(profiles.findByLogin("nkeng-dev")).thenReturn(Optional.of(new ProfileCard("nkeng-dev", snapshot,
                new PublicProfile("CM", List.of(), null, null, "Yaoundé", "Backend PHP", "https://www.linkedin.com/in/nkeng-tabi", true))));

        mvc.perform(get("/api/profiles/nkeng-dev"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nkeng Tabi"))
                .andExpect(jsonPath("$.country").value("CM"))
                .andExpect(jsonPath("$.city").value("Yaoundé"))
                .andExpect(jsonPath("$.linkedInUrl").value("https://www.linkedin.com/in/nkeng-tabi"))
                .andExpect(jsonPath("$.indicators.mergedPullRequestsElsewhere").value(14))
                .andExpect(jsonPath("$.indicators.proofs.mergedElsewhere")
                        .value("https://github.com/pulls?q=is%3Apr+is%3Amerged+author%3Ankeng-dev+-user%3Ankeng-dev"));
    }

    @Test
    @DisplayName("an unknown or private profile is the same 404, in the visitor's language")
    void hiddenProfile() throws Exception {
        when(profiles.findByLogin("ghost")).thenReturn(Optional.empty());

        mvc.perform(get("/api/profiles/ghost").header("Accept-Language", "fr"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cette fiche n’existe pas ou n’est pas publique."));
    }

    @Test
    @DisplayName("before any request the status is NONE, without indicators")
    void none() throws Exception {
        when(reports.findMine(MEMBER)).thenReturn(Optional.empty());

        mvc.perform(get("/api/me/analysis").with(member()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NONE"))
                .andExpect(jsonPath("$.indicators").value(nullValue()));
    }

    @Test
    @DisplayName("a ready analysis gives every indicator with the GitHub pages that prove them")
    void ready() throws Exception {
        ContributionSnapshot snapshot = ContributionSnapshot.of(new GitHubActivity(Instant.parse("2017-03-14T08:00:00Z"), "Nkeng",
                null, "Yaoundé", 94, 2_140, 0, List.of(new CalendarDay(LocalDate.parse("2026-11-05"), 3, 2)), List.of(), 14,
                List.of(new ExternalProject("payafrica/momo-sdk", 6))), NOW);
        when(reports.findMine(MEMBER)).thenReturn(Optional.of(new MyAnalysis(AnalysisStatus.READY, false, "nkeng-dev", snapshot)));

        mvc.perform(get("/api/me/analysis").with(member()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.indicators.publicContributions").value(2140))
                .andExpect(jsonPath("$.indicators.activeMonths").value(1))
                .andExpect(jsonPath("$.indicators.months[11].month").value("2026-11"))
                .andExpect(jsonPath("$.indicators.calendar[0].date").value("2026-11-05"))
                .andExpect(jsonPath("$.indicators.externalProjects[0].url")
                        .value("https://github.com/payafrica/momo-sdk/pulls?q=is%3Apr+is%3Amerged+author%3Ankeng-dev"))
                .andExpect(jsonPath("$.indicators.proofs.profile").value("https://github.com/nkeng-dev"));
    }

    @Test
    @DisplayName("asking for a reading uses the login of the session and answers 202")
    void request() throws Exception {
        mvc.perform(post("/api/me/analysis").with(member()).with(csrf()))
                .andExpect(status().isAccepted());

        verify(analysis).requestAnalysis(MEMBER, "nkeng-dev");
    }

    @Test
    @DisplayName("a visitor cannot read or ask for an analysis")
    void visitor() throws Exception {
        mvc.perform(get("/api/me/analysis")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/me/analysis").with(csrf())).andExpect(status().isUnauthorized());
        verifyNoInteractions(reports, analysis);
    }

    private static RequestPostProcessor member() {
        return oauth2Login().oauth2User(new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_MEMBER")), Map.of("id", MEMBER.toString(), "login", "nkeng-dev"), "id"));
    }
}
