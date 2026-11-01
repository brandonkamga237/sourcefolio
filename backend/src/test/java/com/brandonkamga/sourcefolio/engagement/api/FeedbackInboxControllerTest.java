package com.brandonkamga.sourcefolio.engagement.api;

import com.brandonkamga.sourcefolio.engagement.application.FeedbackInbox;
import com.brandonkamga.sourcefolio.engagement.application.FeedbackPage;
import com.brandonkamga.sourcefolio.engagement.application.ReceivedFeedback;
import com.brandonkamga.sourcefolio.engagement.domain.model.Feedback;
import com.brandonkamga.sourcefolio.engagement.domain.model.FeedbackAngle;
import com.brandonkamga.sourcefolio.engagement.domain.model.FeedbackBody;
import com.brandonkamga.sourcefolio.shared.domain.Language;
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
import java.util.UUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FeedbackInboxController.class)
@Import(SecurityTestConfig.class)
class FeedbackInboxControllerTest {

    private static final UUID AUTHOR = UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11");
    private static final Feedback RECEIVED = new Feedback(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "ada",
            FeedbackAngle.MOBILE, FeedbackBody.parse("Le menu déborde sur mon téléphone."), Language.FR, Instant.parse("2026-11-01T10:00:00Z"));

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private FeedbackInbox inbox;

    @Test
    @DisplayName("an author reads the feedback received, with when they last read it and how much is new")
    void received() throws Exception {
        when(inbox.findReceived(AUTHOR, null, 10)).thenReturn(new ReceivedFeedback(new FeedbackPage(List.of(RECEIVED), "next", 4),
                Instant.parse("2026-11-01T09:00:00Z"), 1));

        mvc.perform(get("/api/me/feedback").with(author()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].authorLogin").value("ada"))
                .andExpect(jsonPath("$.items[0].mine").value(false))
                .andExpect(jsonPath("$.nextCursor").value("next"))
                .andExpect(jsonPath("$.total").value(4))
                .andExpect(jsonPath("$.seenAt").value("2026-11-01T09:00:00Z"))
                .andExpect(jsonPath("$.unread").value(1));
    }

    @Test
    @DisplayName("the number of new feedback is read on its own, for the Me tab")
    void unread() throws Exception {
        when(inbox.countUnread(AUTHOR)).thenReturn(3);

        mvc.perform(get("/api/me/feedback/unread").with(author())).andExpect(jsonPath("$.count").value(3));
    }

    @Test
    @DisplayName("marking the feedback as read answers 204")
    void seen() throws Exception {
        mvc.perform(put("/api/me/feedback/seen").with(author()).with(csrf())).andExpect(status().isNoContent());

        verify(inbox).markSeen(AUTHOR);
    }

    @Test
    @DisplayName("a visitor has no inbox: 401")
    void visitor() throws Exception {
        mvc.perform(get("/api/me/feedback/unread")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/me/feedback/seen").with(csrf())).andExpect(status().isUnauthorized());
        verifyNoInteractions(inbox);
    }

    private static RequestPostProcessor author() {
        return oauth2Login().oauth2User(new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_MEMBER")), Map.of("id", AUTHOR.toString()), "id"));
    }
}
