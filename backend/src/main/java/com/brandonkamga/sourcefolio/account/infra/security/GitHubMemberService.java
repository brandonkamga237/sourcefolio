package com.brandonkamga.sourcefolio.account.infra.security;

import com.brandonkamga.sourcefolio.account.application.ExternalIdentity;
import com.brandonkamga.sourcefolio.account.application.Member;
import com.brandonkamga.sourcefolio.account.application.SignInService;
import com.brandonkamga.sourcefolio.account.domain.model.AuthProvider;
import com.brandonkamga.sourcefolio.shared.domain.Language;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * Anti-corruption layer at the end of the GitHub sign-in: GitHub's user JSON becomes an
 * ExternalIdentity, the use case finds or registers the member, and the session keeps a
 * MemberPrincipal. The browser's language is the new member's language.
 */
class GitHubMemberService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> github;
    private final SignInService signIn;

    GitHubMemberService(SignInService signIn) {
        this(signIn, new DefaultOAuth2UserService());
    }

    GitHubMemberService(SignInService signIn, OAuth2UserService<OAuth2UserRequest, OAuth2User> github) {
        this.signIn = signIn;
        this.github = github;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User profile = github.loadUser(request);
        Object id = profile.getAttribute("id");
        String login = profile.getAttribute("login");
        if (id == null || login == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_user_info", "GitHub returned no id or login", null));
        }
        Member member = signIn.signIn(new ExternalIdentity(
                AuthProvider.GITHUB, String.valueOf(id), login, Language.of(LocaleContextHolder.getLocale())));
        return new MemberPrincipal(member.id().value(), member.login());
    }
}
