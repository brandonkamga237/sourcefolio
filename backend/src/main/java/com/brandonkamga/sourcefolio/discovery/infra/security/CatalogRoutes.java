package com.brandonkamga.sourcefolio.discovery.infra.security;

import com.brandonkamga.sourcefolio.shared.security.SecurityCustomizer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;

/** The catalogue and its members are the first thing a visitor sees, account or not. */
@Component
class CatalogRoutes implements SecurityCustomizer {

    @Override
    public void customize(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(routes -> routes.requestMatchers(HttpMethod.GET, "/api/catalog", "/api/catalog/facets", "/api/ranking", "/api/members/*", "/api/rankings", "/api/rankings/facets").permitAll());
    }
}
