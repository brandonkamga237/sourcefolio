package com.brandonkamga.sourcefolio.profile.domain.model;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** An ISO 3166-1 alpha-2 country code ("CM"). Its name is translated at display time. */
public record CountryCode(String value) {

    private static final Set<String> ISO_CODES = Set.copyOf(Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2));

    public CountryCode {
        Objects.requireNonNull(value, "value");
        if (!ISO_CODES.contains(value)) {
            throw new IllegalArgumentException("Not an ISO 3166 country code: " + value);
        }
    }

    public static Optional<CountryCode> parse(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String code = raw.strip().toUpperCase(Locale.ROOT);
        return ISO_CODES.contains(code) ? Optional.of(new CountryCode(code)) : Optional.empty();
    }
}
