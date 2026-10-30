package com.brandonkamga.sourcefolio.discovery.domain.model;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Optional;

/**
 * Where a visitor is in the Discover feed: the day of the shuffle they started on (so the order
 * does not change under them at midnight), the next feed position, and the last key read in
 * each list. Opaque to clients.
 */
public record DiscoverCursor(LocalDate day, long index, String youngAfter, String oldAfter) {

    public static DiscoverCursor start(LocalDate day) {
        return new DiscoverCursor(day, 0, "", "");
    }

    public String encode() {
        String raw = day + "|" + index + "|" + youngAfter + "|" + oldAfter;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    /** A cursor sent back by a client; anything unreadable starts the feed over. */
    public static Optional<DiscoverCursor> decode(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return Optional.empty();
        }
        try {
            String[] parts = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8).split("\\|", -1);
            if (parts.length != 4 || !isKey(parts[2]) || !isKey(parts[3])) {
                return Optional.empty();
            }
            long index = Long.parseLong(parts[1]);
            return index < 0 ? Optional.empty() : Optional.of(new DiscoverCursor(LocalDate.parse(parts[0]), index, parts[2], parts[3]));
        } catch (IllegalArgumentException | DateTimeParseException unreadable) {
            return Optional.empty();
        }
    }

    // Keys are md5 hex digests, or empty before the first page.
    private static boolean isKey(String key) {
        return key.isEmpty() || key.matches("[0-9a-f]{32}");
    }
}
