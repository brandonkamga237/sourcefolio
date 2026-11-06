package com.brandonkamga.sourcefolio.profile.domain.model;

import java.util.Objects;

/**
 * A few words a member writes about themselves, often imported from GitHub. Their own words,
 * never translated, 2 to 160 characters, line breaks folded into spaces.
 */
public record Bio(String value) {

    public static final int MAX = 160;

    public Bio {
        Objects.requireNonNull(value, "value");
        if (value.length() < 2 || value.length() > MAX || !value.equals(clean(value))) {
            throw new IllegalArgumentException("Not a bio: " + value);
        }
    }

    public static Bio parse(String raw) {
        return new Bio(clean(raw));
    }

    private static String clean(String raw) {
        return raw.replaceAll("[\\p{Cntrl}\\s]+", " ").strip();
    }
}
