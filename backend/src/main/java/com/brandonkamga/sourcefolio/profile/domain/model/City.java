package com.brandonkamga.sourcefolio.profile.domain.model;

import java.util.Objects;

/** The city a member gives for the map, in their own words, 2 to 60 characters on one line. */
public record City(String value) {

    public static final int MAX = 60;

    public City {
        Objects.requireNonNull(value, "value");
        if (value.length() < 2 || value.length() > MAX || !value.equals(clean(value))) {
            throw new IllegalArgumentException("Not a city: " + value);
        }
    }

    public static City parse(String raw) {
        return new City(clean(raw));
    }

    private static String clean(String raw) {
        return raw.replaceAll("[\\p{Cntrl}\\s]+", " ").strip();
    }
}
