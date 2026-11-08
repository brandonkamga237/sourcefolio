package com.brandonkamga.sourcefolio.profile.domain.model;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PresentationValuesTest {

    private static final Instant NOW = Instant.parse("2026-11-06T10:00:00Z");
    private static final UUID MEMBER = UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11");

    @Test
    @DisplayName("a pasted LinkedIn URL is reduced to its handle, and the link is rebuilt from it")
    void linkedIn() {
        assertThat(LinkedInHandle.parse("https://www.linkedin.com/in/nkeng-tabi/?trk=x")).contains(new LinkedInHandle("nkeng-tabi"));
        assertThat(LinkedInHandle.parse("fr.linkedin.com/in/nkeng-tabi")).contains(new LinkedInHandle("nkeng-tabi"));
        assertThat(LinkedInHandle.parse(" nkeng-tabi ")).map(LinkedInHandle::url).contains("https://www.linkedin.com/in/nkeng-tabi");
        assertThat(LinkedInHandle.parse("https://evil.example/in/nkeng")).isEmpty();
        assertThat(LinkedInHandle.parse("ab")).isEmpty();
    }

    @Test
    @DisplayName("bio and city fold line breaks and spaces, within their limits")
    void bioAndCity() {
        assertThat(Bio.parse("  Backend PHP\n\net TypeScript ").value()).isEqualTo("Backend PHP et TypeScript");
        assertThatThrownBy(() -> Bio.parse("x".repeat(161))).isInstanceOf(IllegalArgumentException.class);
        assertThat(City.parse(" Yaoundé ").value()).isEqualTo("Yaoundé");
        assertThatThrownBy(() -> City.parse("Y")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("a profile is private until published, publishing twice keeps the first date, and it needs a country")
    void publication() {
        DeveloperProfile profile = DeveloperProfile.blank(MEMBER);
        assertThat(profile.published()).isFalse();
        assertThatThrownBy(() -> profile.publish(NOW)).isInstanceOf(IllegalStateException.class);

        DeveloperProfile published = profile.withCountry(new CountryCode("CM"), NOW).publish(NOW);
        assertThat(published.publishedAt()).isEqualTo(NOW);
        assertThat(published.publish(NOW.plusSeconds(60)).publishedAt()).isEqualTo(NOW);
        assertThat(published.withBio(new Bio("Backend"), NOW.plusSeconds(5)).published()).isTrue();
        assertThat(published.unpublish(NOW.plusSeconds(90)).published()).isFalse();
    }
}
