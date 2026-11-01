package com.brandonkamga.sourcefolio.profile.domain.model;

import java.util.Objects;

/**
 * The line a member writes about themselves, shown under their name ("Front-end React à
 * Douala"). Their own words, never translated; kept on one line, 2 to 80 characters.
 */
public record Headline(String value) {

    public static final int MAX = 80;

    public Headline {
        Objects.requireNonNull(value, "value");
        if (value.length() < 2 || value.length() > MAX || !value.equals(clean(value))) {
            throw new IllegalArgumentException("Not a headline: " + value);
        }
    }

    /** Spaces and line breaks folded into single spaces; invalid when too short or too long. */
    public static Headline parse(String raw) {
        return new Headline(clean(raw));
    }

    private static String clean(String raw) {
        return raw.replaceAll("[\\p{Cntrl}\\s]+", " ").strip();
    }
}
