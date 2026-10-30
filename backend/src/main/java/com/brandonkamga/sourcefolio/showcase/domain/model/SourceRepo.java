package com.brandonkamga.sourcefolio.showcase.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * The public repository a showcase is built from. Same repository, same showcase. Its GitHub
 * stars are a copy, null until first read or while the repository cannot be read.
 */
public record SourceRepo(RepositoryProvider provider, RepositoryCoordinates coordinates, boolean isPublic,
                         Integer stars, Instant starsCheckedAt) {

    /** A repository whose stars have not been read yet. */
    public SourceRepo(RepositoryProvider provider, RepositoryCoordinates coordinates, boolean isPublic) {
        this(provider, coordinates, isPublic, null, null);
    }

    public SourceRepo {
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(coordinates, "coordinates");
    }

    public String url() {
        return provider.repositoryUrl(coordinates);
    }
}
