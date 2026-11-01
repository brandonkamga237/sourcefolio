package com.brandonkamga.sourcefolio.showcase.domain.model;

import java.time.Instant;
import java.util.List;

/**
 * What GitHub says about a repository at one moment: its stars, its own description (null when
 * it has none), when code was last pushed, and its main languages, largest share first.
 */
public record RepositorySnapshot(int stars, String description, Instant pushedAt, List<String> languages) {

    /** The panel shows a handful of languages; the long tail says little about a portfolio. */
    public static final int MAX_LANGUAGES = 4;

    public RepositorySnapshot {
        languages = List.copyOf(languages.subList(0, Math.min(languages.size(), MAX_LANGUAGES)));
        description = description == null || description.isBlank() ? null : description.strip();
        if (description != null && description.length() > 350) {
            description = description.substring(0, 349) + "…";
        }
    }
}
