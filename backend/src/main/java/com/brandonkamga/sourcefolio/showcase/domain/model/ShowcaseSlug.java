package com.brandonkamga.sourcefolio.showcase.domain.model;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * The readable part of a showcase's public address: /fr/p/nekwalla. Lowercase letters, digits
 * and hyphens, the characters of a GitHub login, so it never needs escaping in a URL.
 */
public record ShowcaseSlug(String value) {

    private static final Pattern SHAPE = Pattern.compile("[a-z0-9](?:[a-z0-9-]{0,98}[a-z0-9])?");

    public ShowcaseSlug {
        Objects.requireNonNull(value, "value");
        if (!SHAPE.matcher(value).matches()) {
            throw new IllegalArgumentException("Not a showcase slug: " + value);
        }
    }

    /** A slug as it arrives in a URL; anything else names no showcase. */
    public static Optional<ShowcaseSlug> parse(String raw) {
        return raw != null && SHAPE.matcher(raw).matches() ? Optional.of(new ShowcaseSlug(raw)) : Optional.empty();
    }

    /** The member's login, lowercased: GitHub logins already have the right shape. */
    public static ShowcaseSlug fromLogin(String login) {
        return new ShowcaseSlug(login.toLowerCase(Locale.ROOT));
    }

    /** The next slug to try when this one is taken: nekwalla-2, nekwalla-3… */
    public ShowcaseSlug withSuffix(int number) {
        return new ShowcaseSlug(value + "-" + number);
    }

    @Override
    public String toString() {
        return value;
    }
}
