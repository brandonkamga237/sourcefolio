package com.brandonkamga.sourcefolio.engagement.api;

import com.brandonkamga.sourcefolio.engagement.application.FeedbackPage;
import com.brandonkamga.sourcefolio.engagement.application.Feedbacks;
import com.brandonkamga.sourcefolio.engagement.domain.model.Feedback;
import com.brandonkamga.sourcefolio.engagement.domain.model.FeedbackAngle;
import com.brandonkamga.sourcefolio.engagement.domain.model.FeedbackBody;
import com.brandonkamga.sourcefolio.engagement.infra.security.FeedbackRoutesTestConfig;
import com.brandonkamga.sourcefolio.shared.domain.Language;
import com.brandonkamga.sourcefolio.shared.exception.BadRequestException;
import com.brandonkamga.sourcefolio.shared.exception.ForbiddenException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FeedbackController.class)
@Import({SecurityTestConfig.class, FeedbackRoutesTestConfig.class})
class FeedbackControllerTest {

    private static final UUID MEMBER = UUID.fromString("5d1e7a40-2b9c-4c55-8f0e-6a3b2c1d0e99");
    private static final UUID OTHER = UUID.fromString("9a0e3f11-6b7c-4d2e-8f90-1a2b3c4d5e6f");
    private static final String BODY = "The hero loads fast, but the contact link is hard to find on a phone.";
    private static final Feedback MINE = new Feedback(UUID.fromString("1f2e3d4c-5b6a-4978-8695-a4b3c2d1e0f9"),
            UUID.randomUUID(), MEMBER, "ada", FeedbackAngle.MOBILE, new FeedbackBody(BODY), Language.FR,
            Instant.parse("2026-10-30T09:00:00Z"));
    private static final Feedback THEIRS = new Feedback(UUID.randomUUID(), MINE.showcaseId(), OTHER, "bola", null,
            new FeedbackBody(BODY), Language.EN, Instant.parse("2026-10-29T09:00:00Z"));

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private Feedbacks feedbacks;

    @Test
    @DisplayName("a visitor reads a page of feedback, without any marked as theirs")
    void visitorReads() throws Exception {
        when(feedbacks.list("nekwalla", null, 10)).thenReturn(new FeedbackPage(List.of(MINE, THEIRS), "next", 7));

        mvc.perform(get("/api/showcases/nekwalla/feedback"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(7))
                .andExpect(jsonPath("$.nextCursor").value("next"))
                .andExpect(jsonPath("$.items[0].authorLogin").value("ada"))
                .andExpect(jsonPath("$.items[0].angle").value("MOBILE"))
                .andExpect(jsonPath("$.items[0].language").value("fr"))
                .andExpect(jsonPath("$.items[0].mine").value(false))
                .andExpect(jsonPath("$.items[1].angle").doesNotExist());
    }

    @Test
    @DisplayName("a member sees which feedback is theirs, and the cursor and size are passed on")
    void memberSeesMine() throws Exception {
        when(feedbacks.list("nekwalla", "abc", 5)).thenReturn(new FeedbackPage(List.of(MINE, THEIRS), null, 2));

        mvc.perform(get("/api/showcases/nekwalla/feedback?cursor=abc&size=5").with(member()))
                .andExpect(jsonPath("$.items[0].mine").value(true))
                .andExpect(jsonPath("$.items[1].mine").value(false));
    }

    @Test
    @DisplayName("a member posts feedback in the language of the page they wrote it on: 201")
    void memberPosts() throws Exception {
        when(feedbacks.post(MEMBER, "nekwalla", "mobile", BODY, Language.FR)).thenReturn(MINE);

        mvc.perform(post("/api/showcases/nekwalla/feedback").with(member()).with(csrf()).header("Accept-Language", "en")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"angle\":\"mobile\",\"body\":\"" + BODY + "\",\"language\":\"fr\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mine").value(true));
    }

    @Test
    @DisplayName("without a language in the body, the request's language is used")
    void postFallsBackToRequestLanguage() throws Exception {
        when(feedbacks.post(MEMBER, "nekwalla", null, BODY, Language.FR)).thenReturn(MINE);

        mvc.perform(post("/api/showcases/nekwalla/feedback").with(member()).with(csrf()).header("Accept-Language", "fr")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"body\":\"" + BODY + "\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("refusals come back as 400 or 403 in the member's language")
    void refusals() throws Exception {
        when(feedbacks.post(MEMBER, "nekwalla", null, "short", Language.FR)).thenThrow(new BadRequestException("feedback.body-invalid"));
        when(feedbacks.post(MEMBER, "mine", null, BODY, Language.FR)).thenThrow(new ForbiddenException("feedback.own-showcase"));

        mvc.perform(post("/api/showcases/nekwalla/feedback").with(member()).with(csrf()).header("Accept-Language", "fr")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"body\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ton retour fait de 10 à 1 000 caractères."));
        mvc.perform(post("/api/showcases/mine/feedback").with(member()).with(csrf()).header("Accept-Language", "fr")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"body\":\"" + BODY + "\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Tu ne peux pas laisser de retour sur ton propre portfolio."));
    }

    @Test
    @DisplayName("a member removes their feedback: 204")
    void remove() throws Exception {
        mvc.perform(delete("/api/feedback/" + MINE.id()).with(member()).with(csrf())).andExpect(status().isNoContent());

        verify(feedbacks).delete(MEMBER, MINE.id());
    }

    @Test
    @DisplayName("a visitor can neither post nor remove: 401")
    void visitorCannotWrite() throws Exception {
        mvc.perform(post("/api/showcases/nekwalla/feedback").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"body\":\"" + BODY + "\"}")).andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/feedback/" + MINE.id()).with(csrf())).andExpect(status().isUnauthorized());
        verifyNoInteractions(feedbacks);
    }

    private static RequestPostProcessor member() {
        return oauth2Login().oauth2User(new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_MEMBER")), Map.of("id", MEMBER.toString()), "id"));
    }
}
