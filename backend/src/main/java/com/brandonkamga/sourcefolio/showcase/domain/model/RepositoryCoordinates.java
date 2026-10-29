package com.brandonkamga.sourcefolio.showcase.domain.model;

import java.util.Objects;

/** A public repository at a provider, by owner and name ("nekwalla/portfolio"). */
public record RepositoryCoordinates(String owner, String name) {

    public RepositoryCoordinates {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(name, "name");
    }

    @Override
    public String toString() {
        return owner + "/" + name;
    }
}
