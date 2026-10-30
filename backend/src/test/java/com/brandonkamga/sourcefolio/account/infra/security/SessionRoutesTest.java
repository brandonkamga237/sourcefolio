package com.brandonkamga.sourcefolio.account.infra.security;

import com.brandonkamga.sourcefolio.shared.security.SecurityTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SessionRoutesTest.NoController.class)
@Import({SessionRoutesTest.NoController.class, SessionRoutes.class, SecurityTestConfig.class})
class SessionRoutesTest {

    // These tests exercise security filters, not controllers: an empty controller of their own
    // keeps the slice from loading every controller of the application.
    @RestController
    static class NoController {
    }

    @Autowired
    private MockMvc mvc;


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
