package com.brandonkamga.sourcefolio.account.infra.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import static org.assertj.core.api.Assertions.assertThat;

class ReturnPathTest {

    @ParameterizedTest
    @ValueSource(strings = {"/", "/fr/p/nekwalla", "/en/p/nekwalla?like=1"})
    @DisplayName("a path on this site is kept")
    void keepsLocalPaths(String path) {
        assertThat(ReturnPath.safe(path)).contains(path);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "https://evil.example", "//evil.example", "/\\evil.example", "fr/p/x", "/fr\n/p", "javascript:alert(1)"})
    @DisplayName("anything that could leave the site is refused: sign-in is never an open redirect")
    void refusesOtherTargets(String path) {
        assertThat(ReturnPath.safe(path)).isEmpty();
    }

    @Test
    @DisplayName("after sign-in the member goes back to the remembered page once, then home")
    void redirectsOnce() throws Exception {
        MockHttpSession session = new MockHttpSession();
        MockHttpServletRequest start = new MockHttpServletRequest("GET", "/oauth2/authorization/github");
        start.setSession(session);
        start.setParameter(ReturnPath.PARAMETER, "/fr/p/nekwalla?like=1");
        new ReturnPath.Remember().doFilter(start, new MockHttpServletResponse(), (request, response) -> { });

        MockHttpServletRequest back = new MockHttpServletRequest("GET", "/login/oauth2/code/github");
        back.setSession(session);
        MockHttpServletResponse first = new MockHttpServletResponse();
        new ReturnPath.Redirect().onAuthenticationSuccess(back, first, null);
        MockHttpServletResponse second = new MockHttpServletResponse();
        new ReturnPath.Redirect().onAuthenticationSuccess(back, second, null);

        assertThat(first.getRedirectedUrl()).isEqualTo("/fr/p/nekwalla?like=1");
        assertThat(second.getRedirectedUrl()).isEqualTo("/");
    }

    @Test
    @DisplayName("an unsafe return path is not remembered")
    void ignoresUnsafeStart() throws Exception {
        MockHttpSession session = new MockHttpSession();
        MockHttpServletRequest start = new MockHttpServletRequest("GET", "/oauth2/authorization/github");
        start.setSession(session);
        start.setParameter(ReturnPath.PARAMETER, "https://evil.example");
        new ReturnPath.Remember().doFilter(start, new MockHttpServletResponse(), (request, response) -> { });

        MockHttpServletRequest back = new MockHttpServletRequest("GET", "/login/oauth2/code/github");
        back.setSession(session);
        MockHttpServletResponse response = new MockHttpServletResponse();
        new ReturnPath.Redirect().onAuthenticationSuccess(back, response, null);

        assertThat(response.getRedirectedUrl()).isEqualTo("/");
    }
}
