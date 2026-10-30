package com.brandonkamga.sourcefolio.account.infra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Brings a visitor back to the page they signed in from (spec: liking or commenting starts the
 * sign-in, then the action runs on return). The page is passed as ?returnTo= when the sign-in
 * starts and kept in the session across the GitHub round trip. Only a path on this site is
 * accepted: anything else would turn sign-in into an open redirect.
 */
final class ReturnPath {

    static final String PARAMETER = "returnTo";
    private static final String ATTRIBUTE = ReturnPath.class.getName();
    private static final Pattern SAFE = Pattern.compile("/(?![/\\\\])[^\\s\\\\]{0,500}");

    private ReturnPath() {
    }

    static Optional<String> safe(String candidate) {
        return candidate != null && SAFE.matcher(candidate).matches() ? Optional.of(candidate) : Optional.empty();
    }

    /** Remembers the page when the sign-in starts, before Spring redirects to GitHub. */
    static final class Remember extends OncePerRequestFilter {

        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            return !request.getRequestURI().startsWith("/oauth2/authorization/");
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            safe(request.getParameter(PARAMETER)).ifPresent(path -> request.getSession().setAttribute(ATTRIBUTE, path));
            chain.doFilter(request, response);
        }
    }

    /** Sends the new member back to that page, or home when there is none. */
    static final class Redirect extends SimpleUrlAuthenticationSuccessHandler {

        Redirect() {
            super("/");
        }

        @Override
        protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return "/";
            }
            Object saved = session.getAttribute(ATTRIBUTE);
            session.removeAttribute(ATTRIBUTE);
            return saved instanceof String path ? safe(path).orElse("/") : "/";
        }
    }
}
