package com.brandonkamga.sourcefolio.account.infra.security;

import com.brandonkamga.sourcefolio.account.application.SignInService;
import com.brandonkamga.sourcefolio.shared.security.SecurityTestConfig;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GitHubLoginTest.NoController.class, properties = {"app.github.client-id=test-client", "app.github.client-secret=test-secret"})
@Import({GitHubLoginTest.NoController.class, GitHubLogin.class, SecurityTestConfig.class})
class GitHubLoginTest {

    // These tests exercise security filters, not controllers: an empty controller of their own
    // keeps the slice from loading every controller of the application.
    @RestController
    static class NoController {
    }

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private SignInService signIn;

    @Test
    @DisplayName("signing in sends the visitor to GitHub asking for the public profile only")
    void redirectsToGitHubWithReadUserOnly() throws Exception {
        mvc.perform(get("/oauth2/authorization/github"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", allOf(
                        startsWith("https://github.com/login/oauth/authorize"),
                        containsString("client_id=test-client"),
                        containsString("scope=read:user"),
                        not(containsString("repo")),
                        not(containsString("read:org")),
                        not(containsString("prompt=")))));
    }

    @Test
    @DisplayName("after signing out, the next sign-in asks GitHub for its account picker, so another account can be used")
    void asksForTheAccountPickerAfterSigningOut() throws Exception {
        mvc.perform(get("/oauth2/authorization/github").cookie(new Cookie(AccountPicker.COOKIE, "1")))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", allOf(
                        startsWith("https://github.com/login/oauth/authorize"),
                        containsString("prompt=select_account"),
                        containsString("scope=read:user"))));
    }
}
