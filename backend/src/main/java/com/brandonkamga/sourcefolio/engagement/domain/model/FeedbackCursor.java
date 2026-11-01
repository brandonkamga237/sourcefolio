package com.brandonkamga.sourcefolio.engagement.domain.model;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/** The last piece of feedback read, newest first. Opaque to clients. */
public record FeedbackCursor(Instant createdAt, UUID id) {

    public String encode() {
        String raw = createdAt + "|" + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static Optional<FeedbackCursor> decode(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return Optional.empty();
        }
        try {
            String[] parts = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8).split("\\|", -1);
            return parts.length == 2
                    ? Optional.of(new FeedbackCursor(Instant.parse(parts[0]), UUID.fromString(parts[1])))
                    : Optional.empty();
        } catch (IllegalArgumentException | DateTimeParseException unreadable) {
            return Optional.empty();
        }
    }
}
