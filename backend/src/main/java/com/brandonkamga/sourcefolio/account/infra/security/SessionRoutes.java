package com.brandonkamga.sourcefolio.account.infra.security;

import com.brandonkamga.sourcefolio.shared.security.SecurityCustomizer;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.stereotype.Component;

/** Signing out ends the server session; the frontend calls it and refreshes, no redirect. */
@Component
class SessionRoutes implements SecurityCustomizer {

    @Override
    public void customize(HttpSecurity http) throws Exception {
        http.logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)));
    }
}
