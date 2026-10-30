package com.brandonkamga.sourcefolio.showcase.infra.security;

import com.brandonkamga.sourcefolio.shared.security.SecurityCustomizer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;

/** A showcase page is opened by visitors from a shared link, without an account. */
@Component
class PublicShowcaseRoutes implements SecurityCustomizer {

    @Override
    public void customize(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(routes -> routes.requestMatchers(HttpMethod.GET, "/api/showcases/*").permitAll());
    }
}
