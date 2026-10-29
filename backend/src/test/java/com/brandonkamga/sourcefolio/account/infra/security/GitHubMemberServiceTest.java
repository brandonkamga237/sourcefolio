package com.brandonkamga.sourcefolio.account.infra.security;

import com.brandonkamga.sourcefolio.account.application.ExternalIdentity;
import com.brandonkamga.sourcefolio.account.application.Member;
import com.brandonkamga.sourcefolio.account.application.SignInService;
import com.brandonkamga.sourcefolio.account.domain.model.AuthProvider;
import com.brandonkamga.sourcefolio.account.domain.model.UserId;
import com.brandonkamga.sourcefolio.shared.domain.Language;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class GitHubMemberServiceTest {

    private final SignInService signIn = mock(SignInService.class);

    @AfterEach
    void resetLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    @DisplayName("GitHub's numeric id identifies the member, the browser language becomes theirs, the session keeps our id")
    void mapsGitHubProfileToMember() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("fr-CM"));
        UserId id = UserId.newId();
        when(signIn.signIn(new ExternalIdentity(AuthProvider.GITHUB, "42", "nekwalla", Language.FR)))
                .thenReturn(new Member(id, "nekwalla", Language.FR));

        OAuth2User principal = service(Map.of("id", 42, "login", "nekwalla", "email", "n@example.org"))
                .loadUser(mock(OAuth2UserRequest.class));

        assertThat(principal.getName()).isEqualTo(id.toString());
        assertThat(principal.getAttributes()).containsOnlyKeys("id", "login");
    }

    @Test
    @DisplayName("a GitHub answer without id or login is refused before any account is touched")
    void incompleteProfileIsRefused() {
        assertThatThrownBy(() -> service(Map.of("login", "nekwalla")).loadUser(mock(OAuth2UserRequest.class)))
                .isInstanceOf(OAuth2AuthenticationException.class);
        verifyNoInteractions(signIn);
    }

    private GitHubMemberService service(Map<String, Object> gitHubAttributes) {
        String nameKey = gitHubAttributes.containsKey("id") ? "id" : "login";
        OAuth2User profile = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority("OAUTH2_USER")), gitHubAttributes, nameKey);
        return new GitHubMemberService(signIn, request -> profile);
    }
}
