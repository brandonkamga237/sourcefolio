package com.brandonkamga.sourcefolio.showcase.domain.model;

import java.util.Objects;

/** The public repository a showcase is built from. Same repository, same showcase. */
public record SourceRepo(RepositoryProvider provider, RepositoryCoordinates coordinates, boolean isPublic) {

    public SourceRepo {
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(coordinates, "coordinates");
    }

    public String url() {
        return provider.repositoryUrl(coordinates);
    }
}
