package com.brandonkamga.sourcefolio.engagement.api;

import com.brandonkamga.sourcefolio.engagement.application.LikeState;
import com.brandonkamga.sourcefolio.engagement.application.Likes;
import com.brandonkamga.sourcefolio.engagement.infra.security.LikeRoutesTestConfig;
import com.brandonkamga.sourcefolio.shared.exception.ForbiddenException;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LikeController.class)
@Import({SecurityTestConfig.class, LikeRoutesTestConfig.class})
class LikeControllerTest {

    private static final UUID MEMBER = UUID.fromString("5d1e7a40-2b9c-4c55-8f0e-6a3b2c1d0e99");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private Likes likes;

    @Test
    @DisplayName("a visitor sees the public count, not liked")
    void visitorSeesCount() throws Exception {
        when(likes.findState(null, "nekwalla")).thenReturn(new LikeState(12, false, false));

        mvc.perform(get("/api/showcases/nekwalla/likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(12))
                .andExpect(jsonPath("$.liked").value(false))
                .andExpect(jsonPath("$.own").value(false));
    }

    @Test
    @DisplayName("a member sees whether they like it")
    void memberSeesOwnState() throws Exception {
        when(likes.findState(MEMBER, "nekwalla")).thenReturn(new LikeState(12, true, false));

        mvc.perform(get("/api/showcases/nekwalla/likes").with(member()))
                .andExpect(jsonPath("$.liked").value(true));
    }

    @Test
    @DisplayName("a member likes with PUT and takes it back with DELETE")
    void likeAndUnlike() throws Exception {
        when(likes.like(MEMBER, "nekwalla")).thenReturn(new LikeState(13, true, false));
        when(likes.unlike(MEMBER, "nekwalla")).thenReturn(new LikeState(12, false, false));

        mvc.perform(put("/api/showcases/nekwalla/like").with(member()).with(csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.count").value(13));
        mvc.perform(delete("/api/showcases/nekwalla/like").with(member()).with(csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.liked").value(false));
    }

    @Test
    @DisplayName("liking one's own portfolio is a 403 in the member's language")
    void ownShowcase() throws Exception {
        when(likes.like(MEMBER, "nekwalla")).thenThrow(new ForbiddenException("engagement.own-showcase"));

        mvc.perform(put("/api/showcases/nekwalla/like").with(member()).with(csrf()).header("Accept-Language", "fr"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Tu ne peux pas apprécier ton propre portfolio."));
    }

    @Test
    @DisplayName("a visitor cannot like: 401, so the page sends them to sign in")
    void visitorCannotLike() throws Exception {
        mvc.perform(put("/api/showcases/nekwalla/like").with(csrf())).andExpect(status().isUnauthorized());
        verifyNoInteractions(likes);
    }

    private static RequestPostProcessor member() {
        return oauth2Login().oauth2User(new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_MEMBER")), Map.of("id", MEMBER.toString()), "id"));
    }
}
