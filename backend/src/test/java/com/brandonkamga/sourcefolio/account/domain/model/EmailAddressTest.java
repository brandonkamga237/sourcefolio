package com.brandonkamga.sourcefolio.account.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;

class EmailAddressTest {

    @Test
    @DisplayName("an address is kept as typed, without surrounding spaces")
    void parses() {
        assertThat(EmailAddress.parse("  ayo@example.cm ")).contains(new EmailAddress("ayo@example.cm"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "ayo", "ayo@", "@example.cm", "ayo@example", "a yo@example.cm", "ayo@@example.cm"})
    @DisplayName("anything that is not shaped like an address is refused")
    void refuses(String raw) {
        assertThat(EmailAddress.parse(raw)).isEmpty();
    }

    @Test
    @DisplayName("an address longer than 320 characters is refused")
    void tooLong() {
        assertThat(EmailAddress.parse("a".repeat(310) + "@example.cm")).isEmpty();
    }
}
