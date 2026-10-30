package com.brandonkamga.sourcefolio.showcase.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;

class ShowcaseSlugTest {

    @Test
    @DisplayName("a GitHub login becomes an address once lowercased")
    void fromLogin() {
        assertThat(ShowcaseSlug.fromLogin("Ayo-Dev42")).isEqualTo(new ShowcaseSlug("ayo-dev42"));
    }

    @Test
    @DisplayName("a taken address gets a numbered suffix")
    void suffix() {
        assertThat(new ShowcaseSlug("nekwalla").withSuffix(2)).isEqualTo(new ShowcaseSlug("nekwalla-2"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "Nekwalla", "-nekwalla", "nekwalla-", "nek walla", "nek/walla", "../admin", "nekwalla%2F"})
    @DisplayName("anything outside lowercase letters, digits and inner hyphens names no showcase")
    void parseRefuses(String raw) {
        assertThat(ShowcaseSlug.parse(raw)).isEmpty();
    }

    @Test
    @DisplayName("a well-formed address is read as is")
    void parseAccepts() {
        assertThat(ShowcaseSlug.parse("nekwalla-2")).contains(new ShowcaseSlug("nekwalla-2"));
    }
}
