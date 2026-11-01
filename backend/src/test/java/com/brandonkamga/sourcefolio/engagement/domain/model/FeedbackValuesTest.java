package com.brandonkamga.sourcefolio.engagement.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeedbackValuesTest {

    @Test
    @DisplayName("a body is trimmed and keeps its line breaks; under 10 or over 1,000 characters it is refused")
    void body() {
        assertThat(FeedbackBody.parse("  Le contraste du menu\r\nest trop faible.  ").value()).isEqualTo("Le contraste du menu\nest trop faible.");
        assertThatThrownBy(() -> FeedbackBody.parse("Nice!")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> FeedbackBody.parse("x".repeat(1_001))).isInstanceOf(IllegalArgumentException.class);
        assertThat(FeedbackBody.parse("x".repeat(1_000)).value()).hasSize(1_000);
    }

    @Test
    @DisplayName("the five angles are read in any case, and nothing else")
    void angles() {
        assertThat(FeedbackAngle.parse("mobile")).contains(FeedbackAngle.MOBILE);
        assertThat(FeedbackAngle.parse("COLOUR")).isEmpty();
        assertThat(FeedbackAngle.parse(null)).isEmpty();
    }

    @Test
    @DisplayName("a cursor survives its round trip and an unreadable one is ignored")
    void cursor() {
        FeedbackCursor cursor = new FeedbackCursor(Instant.parse("2026-11-01T10:00:00Z"), UUID.randomUUID());
        assertThat(FeedbackCursor.decode(cursor.encode())).contains(cursor);
        assertThat(FeedbackCursor.decode("not-a-cursor")).isEmpty();
    }
}
