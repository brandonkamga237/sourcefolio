package com.brandonkamga.sourcefolio.discovery.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CatalogFilterTest {

    @Test
    @DisplayName("raw parameters are tidied: blank is absent, text in lower case, country in upper case")
    void parses() {
        assertThat(CatalogFilter.parse("  React ", "cm", " TypeScript ", null)).contains(new CatalogFilter("react", "CM", "TypeScript", null));
        assertThat(CatalogFilter.parse(null, null, null, " devops ")).contains(new CatalogFilter(null, null, null, "devops"));
        assertThat(CatalogFilter.parse(null, null, "C++", null)).contains(new CatalogFilter(null, null, "C++", null));
        assertThat(CatalogFilter.parse(null, null, "Jupyter Notebook", null)).contains(new CatalogFilter(null, null, "Jupyter Notebook", null));
        assertThat(CatalogFilter.parse(" ", "", null, null)).hasValueSatisfying(filter -> assertThat(filter.isEmpty()).isTrue());
    }

    @Test
    @DisplayName("a country that is not two letters, an odd language or a text too long is refused")
    void refuses() {
        assertThat(CatalogFilter.parse(null, "CMR", null, null)).isEmpty();
        assertThat(CatalogFilter.parse(null, null, "'; DROP TABLE x;--", null)).isEmpty();
        assertThat(CatalogFilter.parse("x".repeat(61), null, null, null)).isEmpty();
        assertThat(CatalogFilter.parse(null, null, null, "Dev Ops")).isEmpty();
    }
}
