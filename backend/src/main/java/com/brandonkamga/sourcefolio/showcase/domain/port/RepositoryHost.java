package com.brandonkamga.sourcefolio.showcase.domain.port;

import com.brandonkamga.sourcefolio.showcase.domain.model.PublicRepository;

import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositorySnapshot;
import java.util.List;
import java.util.Optional;

/** Where members keep their code. GitHub in V1. */
public interface RepositoryHost {

    /** The member's own public repositories; empty when the account no longer exists. */
    List<PublicRepository> listPublicRepositories(String login);

    /** Stars, description, last push and main languages; empty when the repository is gone or no longer public. */
    Optional<RepositorySnapshot> snapshot(RepositoryCoordinates repository);
}
