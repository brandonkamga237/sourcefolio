package com.brandonkamga.sourcefolio.profile.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;

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
    @DisplayName("the ten specialties of the spec are read from their slugs, and nothing else")
    void specialties() {
        assertThat(Specialty.values()).hasSize(10);
        assertThat(Specialty.fromSlug("data-ai")).contains(Specialty.DATA_AI);
        assertThat(Specialty.fromSlug("DATA_AI")).isEmpty();
        assertThat(Specialty.fromSlug("wizard")).isEmpty();
    }
}
