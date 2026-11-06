package com.brandonkamga.sourcefolio.profile.domain.model;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The identifier in linkedin.com/in/&lt;handle&gt;. Only the link is kept: LinkedIn is never read.
 * A pasted profile URL is accepted and reduced to its handle.
 */
public record LinkedInHandle(String value) {

    private static final Pattern HANDLE = Pattern.compile("[\\p{L}\\p{N}_-]{3,100}");
    private static final Pattern URL = Pattern.compile("(?i)^(?:https?://)?(?:[a-z]{2,3}\\.)?linkedin\\.com/in/([^/?#]+)/?(?:[?#].*)?$");

    public LinkedInHandle {
        Objects.requireNonNull(value, "value");
        if (!HANDLE.matcher(value).matches()) {
            throw new IllegalArgumentException("Not a LinkedIn handle: " + value);
        }
    }

    public static Optional<LinkedInHandle> parse(String raw) {
        String candidate = raw.strip();
        Matcher url = URL.matcher(candidate);
        if (url.matches()) {
            candidate = url.group(1);
        }
        return HANDLE.matcher(candidate).matches() ? Optional.of(new LinkedInHandle(candidate)) : Optional.empty();
    }

    public String url() {
        return "https://www.linkedin.com/in/" + value;
    }
}
