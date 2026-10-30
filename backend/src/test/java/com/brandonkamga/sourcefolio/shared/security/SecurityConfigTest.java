package com.brandonkamga.sourcefolio.shared.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityConfigTest.Probe.class)
@Import({SecurityConfigTest.Probe.class, SecurityConfig.class, com.brandonkamga.sourcefolio.shared.web.LocaleTestConfig.class})
class SecurityConfigTest {

    @RestController
    static class Probe {
        @GetMapping("/api/probe")
        String read() {
            return "ok";
        }

        @PostMapping("/api/probe")
        String write() {
            return "written";
        }

        @GetMapping("/elsewhere")
        String elsewhere() {
            return "should not be reachable";
        }
    }

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("an API route without a session answers 401 with the usual error body, in the caller's language")
    void anonymousGets401() throws Exception {
        mvc.perform(get("/api/probe").header("Accept-Language", "fr"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.message").value("Connecte-toi avec GitHub pour continuer."));
    }

    @Test
    @WithMockUser
    @DisplayName("a signed-in member reads an API route")
    void memberReads() throws Exception {
        mvc.perform(get("/api/probe")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("a write without the CSRF token is refused, with it it goes through")
    void writesNeedTheCsrfToken() throws Exception {
        mvc.perform(post("/api/probe")).andExpect(status().isForbidden());
        mvc.perform(post("/api/probe").with(csrf())).andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("a route nobody opened is closed, even with a session")
    void unknownRoutesAreDenied() throws Exception {
        mvc.perform(get("/elsewhere")).andExpect(status().isForbidden());
    }
}
