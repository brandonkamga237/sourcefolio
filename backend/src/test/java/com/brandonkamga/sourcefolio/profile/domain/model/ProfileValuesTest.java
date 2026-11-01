package com.brandonkamga.sourcefolio.profile.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfileValuesTest {

    @Test
    @DisplayName("a country code is read in any case and kept uppercase")
    void countryParses() {
        assertThat(CountryCode.parse("cm")).contains(new CountryCode("CM"));
        assertThat(CountryCode.parse(" CI ")).contains(new CountryCode("CI"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "C", "CMR", "XX", "Cameroun"})
    @DisplayName("anything that is not an ISO 3166 alpha-2 code is not a country")
    void countryRefuses(String raw) {
        assertThat(CountryCode.parse(raw)).isEmpty();
    }

    @Test
    @DisplayName("the fifty interests are read from their slugs, and nothing else")
    void specialties() {
        assertThat(Specialty.values()).hasSize(50);
        assertThat(Specialty.fromSlug("generative-ai")).contains(Specialty.GENERATIVE_AI);
        assertThat(Specialty.fromSlug("data-ai")).contains(Specialty.DATA_AI);
        assertThat(Specialty.fromSlug("DATA_AI")).isEmpty();
        assertThat(Specialty.fromSlug("wizard")).isEmpty();
    }

    @Test
    @DisplayName("up to four interests keep the member's order")
    void rankedSpecialties() {
        Specialties ranked = new Specialties(List.of(Specialty.BACKEND, Specialty.DEVOPS, Specialty.FRONTEND, Specialty.CLOUD));

        assertThat(ranked.slugs()).containsExactly("backend", "devops", "frontend", "cloud");
        assertThat(Specialties.none().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("a fifth interest, or one named twice, is refused")
    void rankedSpecialtiesRefused() {
        assertThatThrownBy(() -> new Specialties(List.of(Specialty.BACKEND, Specialty.DEVOPS, Specialty.FRONTEND, Specialty.MOBILE, Specialty.CLOUD)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Specialties(List.of(Specialty.BACKEND, Specialty.BACKEND)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
