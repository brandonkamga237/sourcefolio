package com.brandonkamga.sourcefolio.account.api;

import com.brandonkamga.sourcefolio.account.application.Member;
import com.brandonkamga.sourcefolio.account.application.SignInService;
import com.brandonkamga.sourcefolio.account.domain.model.UserId;
import com.brandonkamga.sourcefolio.shared.domain.Language;
import com.brandonkamga.sourcefolio.shared.security.SecurityTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeController.class)
@Import(SecurityTestConfig.class)
class MeControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private SignInService members;

    @Test
    @DisplayName("a signed-in member gets their id, login and language")
    void memberGetsTheirProfile() throws Exception {
        UUID id = UUID.randomUUID();
        when(members.requireMember(new UserId(id))).thenReturn(new Member(new UserId(id), "nekwalla", Language.FR));

        // The session principal's name is the member id, whatever class carries it.
        OAuth2User principal = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority("ROLE_MEMBER")), Map.of("id", id.toString()), "id");

        mvc.perform(get("/api/me").with(oauth2Login().oauth2User(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.login").value("nekwalla"))
                .andExpect(jsonPath("$.language").value("fr"));
    }

    @Test
    @DisplayName("a visitor gets 401, which the frontend reads as 'show the visitor screens'")
    void visitorGets401() throws Exception {
        mvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }
}
