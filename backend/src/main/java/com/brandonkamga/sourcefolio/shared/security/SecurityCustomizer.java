package com.brandonkamga.sourcefolio.shared.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * How a module adds its own routes and filters to the one API filter chain (sign-in, logout,
 * public catalogue routes) without shared knowing the module. Customizers run before the
 * general rules, so the routes they open take precedence over "/api/** needs a session".
 */
public interface SecurityCustomizer {

    void customize(HttpSecurity http) throws Exception;
}
