package com.brandonkamga.sourcefolio.account.infra.security;

import com.brandonkamga.sourcefolio.account.application.SignInService;
import com.brandonkamga.sourcefolio.shared.security.SecurityCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

/**
 * GitHub sign-in, switched on only when the OAuth App is configured: without GITHUB_CLIENT_ID the
 * API still starts and every public route works, only signing in is unavailable.
 */
@Configuration
@ConditionalOnExpression("'${app.github.client-id:}' != ''")
class GitHubLogin {

    @Bean
    ClientRegistrationRepository clientRegistrations(@Value("${app.github.client-id}") String clientId,
                                                     @Value("${app.github.client-secret}") String clientSecret) {
        // The public profile only: no repo, no read:org, no email scope.
        return new InMemoryClientRegistrationRepository(CommonOAuth2Provider.GITHUB.getBuilder("github")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .scope("read:user")
                .build());
    }

    @Bean
    SecurityCustomizer gitHubLoginRoutes(SignInService signIn) {
        GitHubMemberService members = new GitHubMemberService(signIn);
        return http -> http
                .authorizeHttpRequests(routes -> routes.requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll())
                .oauth2Login(login -> login
                        .userInfoEndpoint(userInfo -> userInfo.userService(members))
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/?signin=failed"));
    }
}
