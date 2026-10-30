package com.brandonkamga.sourcefolio.discovery.domain.model;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/** The last card read in the Recent list, newest first. Opaque to clients. */
public record RecentCursor(Instant publishedAt, UUID showcaseId) {

    public String encode() {
        String raw = publishedAt + "|" + showcaseId;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static Optional<RecentCursor> decode(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return Optional.empty();
        }
        try {
            String[] parts = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8).split("\\|", -1);
            return parts.length == 2
                    ? Optional.of(new RecentCursor(Instant.parse(parts[0]), UUID.fromString(parts[1])))
                    : Optional.empty();
        } catch (IllegalArgumentException | DateTimeParseException unreadable) {
            return Optional.empty();
        }
    }
}
