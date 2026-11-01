package com.brandonkamga.sourcefolio.account.infra.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Arrays;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

/**
 * After signing out, the next sign-in asks GitHub for its account picker: GitHub otherwise
 * signs the browser straight back into the account it remembers, and a member with a second
 * GitHub account could never reach it. A cookie set on sign-out carries that wish across the
 * round trip; signing in clears it, so an ordinary sign-in keeps its single step.
 */
final class AccountPicker {

    static final String COOKIE = "sf_pick_account";
    private static final Duration KEPT = Duration.ofDays(30);

    private AccountPicker() {
    }

    static void ask(HttpServletResponse response) {
        response.addCookie(cookie("1", (int) KEPT.toSeconds()));
    }

    static void forget(HttpServletResponse response) {
        response.addCookie(cookie("", 0));
    }

    static boolean asked(HttpServletRequest request) {
        return request.getCookies() != null
                && Arrays.stream(request.getCookies()).anyMatch(cookie -> COOKIE.equals(cookie.getName()) && "1".equals(cookie.getValue()));
    }

    /** GitHub's authorization request, with prompt=select_account when the member signed out before. */
    static OAuth2AuthorizationRequestResolver resolver(ClientRegistrationRepository registrations) {
        DefaultOAuth2AuthorizationRequestResolver defaults = new DefaultOAuth2AuthorizationRequestResolver(registrations,
                OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);
        return new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                return withPicker(request, defaults.resolve(request));
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String registrationId) {
                return withPicker(request, defaults.resolve(request, registrationId));
            }
        };
    }

    private static OAuth2AuthorizationRequest withPicker(HttpServletRequest request, OAuth2AuthorizationRequest authorization) {
        if (authorization == null || !asked(request)) {
            return authorization;
        }
        return OAuth2AuthorizationRequest.from(authorization)
                .additionalParameters(parameters -> parameters.put("prompt", "select_account"))
                .build();
    }

    private static Cookie cookie(String value, int maxAge) {
        Cookie cookie = new Cookie(COOKIE, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }
}
