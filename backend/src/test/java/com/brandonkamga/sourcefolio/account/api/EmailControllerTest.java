package com.brandonkamga.sourcefolio.account.api;

import com.brandonkamga.sourcefolio.account.application.AccountSettings;
import com.brandonkamga.sourcefolio.account.application.ChangeEmail;
import com.brandonkamga.sourcefolio.account.domain.model.UserId;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmailController.class)
@Import(SecurityTestConfig.class)
class EmailControllerTest {

    private static final UUID MEMBER = UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AccountSettings settings;

    @Test
    @DisplayName("a member without an address reads null")
    void none() throws Exception {
        when(settings.findEmail(new UserId(MEMBER))).thenReturn(Optional.empty());

        mvc.perform(get("/api/me/email").with(member()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(nullValue()));
    }

    @Test
    @DisplayName("a new address is saved and returned")
    void change() throws Exception {
        when(settings.changeEmail(new ChangeEmail(new UserId(MEMBER), "ayo@example.cm"))).thenReturn(Optional.of("ayo@example.cm"));

        mvc.perform(put("/api/me/email").with(member()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"ayo@example.cm\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ayo@example.cm"));
    }

    @Test
    @DisplayName("an invalid address is a 400 in the member's language")
    void invalid() throws Exception {
        when(settings.changeEmail(new ChangeEmail(new UserId(MEMBER), "nope"))).thenThrow(new BadRequestException("account.email-invalid"));

        mvc.perform(put("/api/me/email").with(member()).with(csrf()).header("Accept-Language", "fr")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"nope\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ce n’est pas une adresse e-mail."));
    }

    private static RequestPostProcessor member() {
        return oauth2Login().oauth2User(new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_MEMBER")), Map.of("id", MEMBER.toString()), "id"));
    }
}
