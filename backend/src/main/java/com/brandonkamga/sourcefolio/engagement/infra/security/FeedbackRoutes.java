package com.brandonkamga.sourcefolio.engagement.infra.security;

import com.brandonkamga.sourcefolio.shared.security.SecurityCustomizer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;

/** Anyone reads a showcase's feedback; writing and removing it need a session. */
@Component
class FeedbackRoutes implements SecurityCustomizer {

    @Override
    public void customize(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(routes -> routes.requestMatchers(HttpMethod.GET, "/api/showcases/*/feedback").permitAll());
    }
}
