package com.brandonkamga.sourcefolio.discovery.domain.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/** The one indicator a ranking sorts on; each says exactly what it measures. */
public enum RankingSort {
    /** Active months out of twelve, then public contributions. */
    REGULARITY,
    /** Public contributions over twelve months. */
    CONTRIBUTIONS,
    /** Stars received on owned public repositories. */
    STARS,
    /** Pull requests merged in other people's repositories. */
    EXTERNAL,
    /** Oldest GitHub account first. */
    SENIORITY;

    public static Optional<RankingSort> parse(String raw) {
        return Arrays.stream(values()).filter(sort -> sort.name().equals(raw.strip().toUpperCase(Locale.ROOT))).findFirst();
    }
}
