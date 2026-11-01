package com.brandonkamga.sourcefolio.showcase.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * The public repository a showcase is built from. Same repository, same showcase. What GitHub
 * says about it (stars, description, main languages, last push) is a copy, read at most once a
 * day: null or empty until the first read, or while the repository cannot be read.
 */
public record SourceRepo(RepositoryProvider provider, RepositoryCoordinates coordinates, boolean isPublic,
                         Integer stars, String description, List<String> languages, Instant pushedAt, Instant starsCheckedAt) {

    /** A repository GitHub has not been asked about yet. */
    public SourceRepo(RepositoryProvider provider, RepositoryCoordinates coordinates, boolean isPublic) {
        this(provider, coordinates, isPublic, null, null, List.of(), null, null);
    }

    public SourceRepo {
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(coordinates, "coordinates");
        languages = languages == null ? List.of() : List.copyOf(languages);
    }

    public String url() {
        return provider.repositoryUrl(coordinates);
    }
}
