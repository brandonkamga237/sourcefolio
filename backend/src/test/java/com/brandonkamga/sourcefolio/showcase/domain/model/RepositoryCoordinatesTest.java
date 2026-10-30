package com.brandonkamga.sourcefolio.showcase.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;

class RepositoryCoordinatesTest {

    @Test
    @DisplayName("owner/name is read as a repository")
    void parses() {
        assertThat(RepositoryCoordinates.parse(" nekwalla/portfolio ")).contains(new RepositoryCoordinates("nekwalla", "portfolio"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "portfolio", "nekwalla/", "/portfolio", "a/b/c"})
    @DisplayName("anything that is not exactly owner/name is not a repository")
    void refuses(String raw) {
        assertThat(RepositoryCoordinates.parse(raw)).isEmpty();
    }

    @Test
    @DisplayName("GitHub names ignore case")
    void sameAsIgnoresCase() {
        assertThat(new RepositoryCoordinates("Nekwalla", "Portfolio").sameAs(new RepositoryCoordinates("nekwalla", "portfolio"))).isTrue();
        assertThat(new RepositoryCoordinates("nekwalla", "site").sameAs(new RepositoryCoordinates("nekwalla", "portfolio"))).isFalse();
    }
}
