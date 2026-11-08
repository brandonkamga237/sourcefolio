package com.brandonkamga.sourcefolio.discovery.infra.security;

import com.brandonkamga.sourcefolio.shared.security.SecurityCustomizer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;

/** Rankings are open to every visitor, account or not. */
@Component
class RankingRoutes implements SecurityCustomizer {

    @Override
    public void customize(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(routes -> routes.requestMatchers(HttpMethod.GET, "/api/rankings", "/api/rankings/facets", "/api/rankings/cities").permitAll());
    }
}
