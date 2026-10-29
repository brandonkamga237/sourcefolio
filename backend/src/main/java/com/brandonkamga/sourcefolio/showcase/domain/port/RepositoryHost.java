package com.brandonkamga.sourcefolio.showcase.domain.port;

import com.brandonkamga.sourcefolio.showcase.domain.model.PublicRepository;

import java.util.List;

/** Where members keep their code. GitHub in V1. */
public interface RepositoryHost {

    /** The member's own public repositories; empty when the account no longer exists. */
    List<PublicRepository> listPublicRepositories(String login);
}
