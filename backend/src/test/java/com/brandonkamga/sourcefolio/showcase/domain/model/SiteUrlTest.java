package com.brandonkamga.sourcefolio.showcase.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SiteUrlTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "example.com, https://example.com/",
            "  https://nekwalla.dev  , https://nekwalla.dev/",
            "HTTPS://Example.COM/work, https://example.com/work",
            "http://site.example.org:8080/a/, http://site.example.org:8080/a/",
            "https://gehmit.org/, https://gehmit.org/",
    })
    @DisplayName("a homepage becomes the address a visitor would open")
    void normalises(String raw, String expected) {
        assertThat(SiteUrl.parse(raw)).map(SiteUrl::value).contains(expected);
    }

    @ParameterizedTest(name = "\"{0}\" is not a site")
    @ValueSource(strings = {"", "   ", "TBD", "javascript:alert(1)", "ftp://files.example.org", "localhost", "https://", "mailto:me@example.org", "https://paypal.com@evil.example", "nekwalla.dev:443@evil.example"})
    @DisplayName("free text, other schemes and hosts without a domain are not sites")
    void rejects(String raw) {
        assertThat(SiteUrl.parse(raw)).isEmpty();
    }
}
