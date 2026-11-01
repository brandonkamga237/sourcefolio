package com.brandonkamga.sourcefolio.discovery.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CatalogFilterTest {

    @Test
    @DisplayName("raw parameters are tidied: blank is absent, text in lower case, country in upper case")
    void parses() {
        assertThat(CatalogFilter.parse("  React ", "cm", " frontend ")).contains(new CatalogFilter("react", "CM", "frontend"));
        assertThat(CatalogFilter.parse(" ", "", null)).hasValueSatisfying(filter -> assertThat(filter.isEmpty()).isTrue());
    }

    @Test
    @DisplayName("a country that is not two letters, an odd interest or a text too long is refused")
    void refuses() {
        assertThat(CatalogFilter.parse(null, "CMR", null)).isEmpty();
        assertThat(CatalogFilter.parse(null, null, "DROP TABLE")).isEmpty();
        assertThat(CatalogFilter.parse("x".repeat(61), null, null)).isEmpty();
    }
}
