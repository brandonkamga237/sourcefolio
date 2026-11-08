package com.brandonkamga.sourcefolio.contribution.infra.security;

import com.brandonkamga.sourcefolio.shared.security.SecurityCustomizer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;

/** Published profiles are read by anyone, from a shared link or the rankings. */
@Component
class ProfileRoutes implements SecurityCustomizer {

    @Override
    public void customize(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(routes -> routes.requestMatchers(HttpMethod.GET, "/api/profiles/*").permitAll());
    }
}
