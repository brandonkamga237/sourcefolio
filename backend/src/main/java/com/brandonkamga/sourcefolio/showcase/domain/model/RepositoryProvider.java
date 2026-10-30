package com.brandonkamga.sourcefolio.showcase.domain.model;

/** Where a source repository lives. GitHub only in V1; GitLab or Codeberg would be new values. */
public enum RepositoryProvider {
    GITHUB("https://github.com/");

    private final String origin;

    RepositoryProvider(String origin) {
        this.origin = origin;
    }

    public String repositoryUrl(RepositoryCoordinates coordinates) {
        return origin + coordinates;
    }
}
