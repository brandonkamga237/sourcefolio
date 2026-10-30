package com.brandonkamga.sourcefolio.showcase.domain.model;

import java.util.Objects;
import java.util.Optional;

/** A public repository at a provider, by owner and name ("nekwalla/portfolio"). */
public record RepositoryCoordinates(String owner, String name) {

    public RepositoryCoordinates {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(name, "name");
    }

    /** Reads "owner/name"; anything else is not a repository. */
    public static Optional<RepositoryCoordinates> parse(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String[] parts = raw.strip().split("/", -1);
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new RepositoryCoordinates(parts[0], parts[1]));
    }

    /** GitHub names ignore case: Nekwalla/Portfolio is nekwalla/portfolio. */
    public boolean sameAs(RepositoryCoordinates other) {
        return owner.equalsIgnoreCase(other.owner) && name.equalsIgnoreCase(other.name);
    }

    @Override
    public String toString() {
        return owner + "/" + name;
    }
}
