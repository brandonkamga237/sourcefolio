package com.brandonkamga.sourcefolio.shared.security;

import com.brandonkamga.sourcefolio.shared.exception.ErrorCode;
import com.brandonkamga.sourcefolio.shared.web.ApiError;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.LocaleResolver;
import tools.jackson.databind.json.JsonMapper;

/**
 * One filter chain for the whole API. Production serves the site and the API on one origin
 * (Traefik routes /api to this process), so there is no CORS, and writes are protected by the
 * session cookie being SameSite=Lax plus the CSRF token the frontend echoes back.
 *
 * `/api/**` needs a session unless a module opens a route through a {@link SecurityCustomizer}.
 */
@Configuration
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    SecurityFilterChain apiSecurity(HttpSecurity http, AuthenticationEntryPoint unauthenticated,
                                    ObjectProvider<SecurityCustomizer> customizers) throws Exception {
        for (SecurityCustomizer customizer : customizers.orderedStream().toList()) {
            customizer.customize(http);
        }
        http
                .authorizeHttpRequests(routes -> routes
                        // The actuator port is internal (never routed by Traefik): health and metrics.
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll())
                // Cookie token readable by the frontend, sent back in X-XSRF-TOKEN on writes.
                .csrf(csrf -> csrf.spa())
                .exceptionHandling(errors -> errors.authenticationEntryPoint(unauthenticated))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);
        return http.build();
    }

    // The same body as every other error, in the caller's language, rather than an empty 401.
    @Bean
    AuthenticationEntryPoint unauthenticated(MessageSource messages, LocaleResolver locales, JsonMapper json) {
        return (request, response, exception) -> {
            String message = messages.getMessage("error.unauthenticated", null, locales.resolveLocale(request));
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            json.writeValue(response.getWriter(), ApiError.of(ErrorCode.UNAUTHENTICATED, message, request.getRequestURI()));
        };
    }
}
