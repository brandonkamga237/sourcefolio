package com.brandonkamga.sourcefolio.showcase.application;

import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import java.util.Objects;
import java.util.UUID;

/** "Yes, publish": the member picks one of their detected candidates by its repository. */
public record PublishShowcase(UUID memberId, RepositoryCoordinates repository) {

    public PublishShowcase {
        Objects.requireNonNull(memberId, "memberId");
        Objects.requireNonNull(repository, "repository");
    }
}
