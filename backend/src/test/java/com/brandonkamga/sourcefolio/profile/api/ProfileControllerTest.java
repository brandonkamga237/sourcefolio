package com.brandonkamga.sourcefolio.profile.api;

import com.brandonkamga.sourcefolio.profile.application.ProfileCompletion;
import com.brandonkamga.sourcefolio.profile.domain.model.CountryCode;
import com.brandonkamga.sourcefolio.profile.domain.model.DeveloperProfile;
import com.brandonkamga.sourcefolio.profile.domain.model.Headline;
import com.brandonkamga.sourcefolio.profile.domain.model.Specialties;
import com.brandonkamga.sourcefolio.profile.domain.model.Specialty;
import com.brandonkamga.sourcefolio.shared.exception.BadRequestException;
import com.brandonkamga.sourcefolio.shared.security.SecurityTestConfig;
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
import java.util.UUID;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
@Import(SecurityTestConfig.class)
class ProfileControllerTest {

    private static final UUID MEMBER = UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ProfileCompletion profiles;

    @Test
    @DisplayName("a blank profile answers null fields")
    void blank() throws Exception {
        when(profiles.findMyProfile(MEMBER)).thenReturn(DeveloperProfile.blank(MEMBER));

        mvc.perform(get("/api/me/profile").with(member()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.country").value(nullValue()))
                .andExpect(jsonPath("$.specialties").isEmpty());
    }

    @Test
    @DisplayName("ranked specialties are saved and the whole profile comes back, as a code and slugs in order")
    void answer() throws Exception {
        when(profiles.answerSpecialties(MEMBER, List.of("data-ai", "backend")))
                .thenReturn(new DeveloperProfile(MEMBER, new CountryCode("CM"), new Specialties(List.of(Specialty.DATA_AI, Specialty.BACKEND)),
                        Instant.parse("2026-10-30T09:00:00Z")));

        mvc.perform(put("/api/me/profile/specialties").with(member()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"specialties\":[\"data-ai\",\"backend\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.country").value("CM"))
                .andExpect(jsonPath("$.specialties[0]").value("data-ai"))
                .andExpect(jsonPath("$.specialties[1]").value("backend"));
    }

    @Test
    @DisplayName("a country outside the list is a 400 in the member's language")
    void invalidCountry() throws Exception {
        when(profiles.answerCountry(MEMBER, "Cameroun")).thenThrow(new BadRequestException("profile.country-invalid"));

        mvc.perform(put("/api/me/profile/country").with(member()).with(csrf()).header("Accept-Language", "fr")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"country\":\"Cameroun\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Choisis un pays dans la liste."));
    }

    @Test
    @DisplayName("a fifth interest is a 400 in the member's language")
    void tooManySpecialties() throws Exception {
        when(profiles.answerSpecialties(MEMBER, List.of("backend", "devops", "frontend", "mobile", "cloud")))
                .thenThrow(new BadRequestException("profile.specialties-invalid"));

        mvc.perform(put("/api/me/profile/specialties").with(member()).with(csrf()).header("Accept-Language", "fr")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"specialties\":[\"backend\",\"devops\",\"frontend\",\"mobile\",\"cloud\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Choisis un à quatre domaines différents."));
    }

    @Test
    @DisplayName("the member's headline is saved and comes back with the profile")
    void headline() throws Exception {
        when(profiles.answerHeadline(MEMBER, "Backend à Douala")).thenReturn(DeveloperProfile.blank(MEMBER)
                .withHeadline(new Headline("Backend à Douala"), Instant.parse("2026-11-01T09:00:00Z")));

        mvc.perform(put("/api/me/profile/headline").with(member()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"headline\":\"Backend à Douala\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headline").value("Backend à Douala"));
    }

    @Test
    @DisplayName("a headline too long is a 400 in the member's language")
    void headlineRefused() throws Exception {
        when(profiles.answerHeadline(MEMBER, "x".repeat(81))).thenThrow(new BadRequestException("profile.headline-invalid"));

        mvc.perform(put("/api/me/profile/headline").with(member()).with(csrf()).header("Accept-Language", "fr")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"headline\":\"" + "x".repeat(81) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ton titre fait entre 2 et 80 caractères, sur une seule ligne."));
    }

    @Test
    @DisplayName("a missing list is refused before reaching the use case")
    void missingSpecialties() throws Exception {
        mvc.perform(put("/api/me/profile/specialties").with(member()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(profiles);
    }

    @Test
    @DisplayName("an empty answer is refused before reaching the use case")
    void emptyAnswer() throws Exception {
        mvc.perform(put("/api/me/profile/country").with(member()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"country\":\"\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(profiles);
    }

    @Test
    @DisplayName("a visitor has no profile to read or answer")
    void visitor() throws Exception {
        mvc.perform(get("/api/me/profile")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/me/profile/country").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"country\":\"CM\"}")).andExpect(status().isUnauthorized());
    }

    private static RequestPostProcessor member() {
        return oauth2Login().oauth2User(new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_MEMBER")), Map.of("id", MEMBER.toString()), "id"));
    }
}
