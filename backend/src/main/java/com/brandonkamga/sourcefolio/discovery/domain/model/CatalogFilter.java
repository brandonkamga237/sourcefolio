package com.brandonkamga.sourcefolio.discovery.domain.model;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * What a visitor narrows Discover or Recent to: words found in the author's login, site or
 * headline, the author's country, one interest. Each part is optional; none means everything.
 * It narrows, never reorders.
 */
public record CatalogFilter(String text, String countryCode, String specialty) {

    public static final int MAX_TEXT = 60;
    private static final Pattern COUNTRY = Pattern.compile("[A-Z]{2}");
    private static final Pattern SLUG = Pattern.compile("[a-z][a-z-]{1,19}");

    public CatalogFilter {
        if (text != null && (text.isBlank() || text.length() > MAX_TEXT)) {
            throw new IllegalArgumentException("Not a search text: " + text);
        }
        if (countryCode != null && !COUNTRY.matcher(countryCode).matches()) {
            throw new IllegalArgumentException("Not a country code: " + countryCode);
        }
        if (specialty != null && !SLUG.matcher(specialty).matches()) {
            throw new IllegalArgumentException("Not an interest: " + specialty);
        }
    }

    public static CatalogFilter none() {
        return new CatalogFilter(null, null, null);
    }

    /** Reads raw query parameters; blank means absent, the text is folded to lower case. Empty when one part is not valid. */
    public static Optional<CatalogFilter> parse(String text, String countryCode, String specialty) {
        try {
            return Optional.of(new CatalogFilter(
                    blank(text) ? null : text.strip().toLowerCase(Locale.ROOT),
                    blank(countryCode) ? null : countryCode.strip().toUpperCase(Locale.ROOT),
                    blank(specialty) ? null : specialty.strip()));
        } catch (IllegalArgumentException invalid) {
            return Optional.empty();
        }
    }

    public boolean isEmpty() {
        return text == null && countryCode == null && specialty == null;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
