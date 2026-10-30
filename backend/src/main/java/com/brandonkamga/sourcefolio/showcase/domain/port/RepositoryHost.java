package com.brandonkamga.sourcefolio.showcase.domain.port;

import com.brandonkamga.sourcefolio.showcase.domain.model.PublicRepository;

import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import java.util.List;
import java.util.OptionalInt;

/** Where members keep their code. GitHub in V1. */
public interface RepositoryHost {

    /** The member's own public repositories; empty when the account no longer exists. */
    List<PublicRepository> listPublicRepositories(String login);

    /** The repository's star count; empty when it is gone or no longer public. */
    OptionalInt stars(RepositoryCoordinates repository);
}
