package com.brandonkamga.sourcefolio.contribution.domain.port;

import com.brandonkamga.sourcefolio.contribution.domain.model.GitHubActivity;
import java.time.Instant;
import java.util.Optional;

/** Public GitHub data of one login. Empty when GitHub does not know the login. */
public interface ActivitySource {

    Optional<GitHubActivity> read(String login, Instant now);
}
