package com.brandonkamga.sourcefolio.showcase.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * What GitHub says about a showcase's repository changed: stars, description, main languages
 * (largest share first), last push. Nulls and an empty list when it can no longer be read.
 */
public record ShowcaseRepositoryChanged(UUID showcaseId, Integer stars, String description, List<String> languages, Instant pushedAt) {

    public ShowcaseRepositoryChanged {
        languages = languages == null ? List.of() : List.copyOf(languages);
    }
}
