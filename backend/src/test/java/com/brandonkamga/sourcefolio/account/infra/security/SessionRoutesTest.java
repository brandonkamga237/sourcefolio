package com.brandonkamga.sourcefolio.account.infra.security;

import com.brandonkamga.sourcefolio.account.application.SignInService;
import com.brandonkamga.sourcefolio.shared.security.SecurityTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({SessionRoutes.class, SecurityTestConfig.class})
class SessionRoutesTest {

    @Autowired
    private MockMvc mvc;

    // The slice loads every controller; MeController needs this collaborator to start.
    @MockitoBean
    private SignInService signIn;

    @Test
    @WithMockUser
    @DisplayName("signing out answers 204 with no redirect, so the frontend just refreshes")
    void logoutAnswersNoContent() throws Exception {
        mvc.perform(post("/api/auth/logout").with(csrf())).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    @DisplayName("signing out without the CSRF token is refused: another site cannot sign a member out")
    void logoutNeedsTheCsrfToken() throws Exception {
        mvc.perform(post("/api/auth/logout")).andExpect(status().isForbidden());
    }
}
