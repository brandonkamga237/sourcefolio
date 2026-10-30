package com.brandonkamga.sourcefolio.discovery.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class CursorsTest {

    @Test
    @DisplayName("a Discover cursor reads back as it was written")
    void discoverRoundTrip() {
        DiscoverCursor cursor = new DiscoverCursor(LocalDate.parse("2026-10-30"), 20,
                "0f3c9a2b4d5e6f708192a3b4c5d6e7f8", "");

        assertThat(DiscoverCursor.decode(cursor.encode())).contains(cursor);
    }

    @Test
    @DisplayName("a Recent cursor reads back as it was written")
    void recentRoundTrip() {
        RecentCursor cursor = new RecentCursor(Instant.parse("2026-10-30T09:00:00Z"), UUID.randomUUID());

        assertThat(RecentCursor.decode(cursor.encode())).contains(cursor);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "not-base64!", "Zm9v"})
    @DisplayName("an unreadable cursor starts the list over instead of failing")
    void garbage(String raw) {
        assertThat(DiscoverCursor.decode(raw)).isEmpty();
        assertThat(RecentCursor.decode(raw)).isEmpty();
    }

    @Test
    @DisplayName("a forged Discover cursor with a key that is not a digest is refused")
    void forgedKey() {
        String forged = Base64.getUrlEncoder().encodeToString("2026-10-30|0|' OR 1=1 --|".getBytes(StandardCharsets.UTF_8));

        assertThat(DiscoverCursor.decode(forged)).isEmpty();
    }
}
