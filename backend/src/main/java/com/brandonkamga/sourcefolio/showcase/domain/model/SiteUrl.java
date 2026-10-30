package com.brandonkamga.sourcefolio.showcase.domain.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * The address of a site as a visitor would open it. Built only from http(s) URLs with a real
 * host name; a repository's homepage field is free text ("example.com", "TBD", "javascript:…"),
 * so anything else is not a site.
 */
public record SiteUrl(String value) {

    private static final Pattern HAS_SCHEME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.-]*:");

    public static Optional<SiteUrl> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String candidate = raw.strip();
        // "example.com" gets https://; "mailto:me@example.org" keeps its scheme and is refused,
        // instead of turning into https://mailto:me@example.org.
        if (!HAS_SCHEME.matcher(candidate).find()) {
            candidate = "https://" + candidate;
        }
        try {
            URI uri = new URI(candidate);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            String host = uri.getHost();
            // Credentials in a URL (https://user@host) are a classic way to disguise where a link goes.
            if (!scheme.equals("http") && !scheme.equals("https") || host == null || !host.contains(".")
                    || uri.getRawUserInfo() != null) {
                return Optional.empty();
            }
            String path = uri.getRawPath() == null || uri.getRawPath().isEmpty() ? "/" : uri.getRawPath();
            String port = uri.getPort() == -1 ? "" : ":" + uri.getPort();
            return Optional.of(new SiteUrl(scheme + "://" + host.toLowerCase(Locale.ROOT) + port + path));
        } catch (URISyntaxException invalid) {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
