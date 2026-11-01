package com.brandonkamga.sourcefolio.discovery.domain.model;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * What a visitor narrows Discover or Recent to: words found in the author's login, site or
 * headline, the author's country, one language of the source repository. Each part is optional;
 * none means everything. It narrows, never reorders. The language is read from GitHub, not
 * declared by the author: what the code is written in, which an interest cannot promise.
 */
public record CatalogFilter(String text, String countryCode, String language) {

    public static final int MAX_TEXT = 60;
    private static final Pattern COUNTRY = Pattern.compile("[A-Z]{2}");
    // GitHub's language names: "TypeScript", "C++", "C#", "Jupyter Notebook", "Vim Script"…
    private static final Pattern LANGUAGE = Pattern.compile("[A-Za-z0-9][A-Za-z0-9+#.' -]{0,39}");

    public CatalogFilter {
        if (text != null && (text.isBlank() || text.length() > MAX_TEXT)) {
            throw new IllegalArgumentException("Not a search text: " + text);
        }
        if (countryCode != null && !COUNTRY.matcher(countryCode).matches()) {
            throw new IllegalArgumentException("Not a country code: " + countryCode);
        }
        if (language != null && !LANGUAGE.matcher(language).matches()) {
            throw new IllegalArgumentException("Not a language: " + language);
        }
    }

    public static CatalogFilter none() {
        return new CatalogFilter(null, null, null);
    }

    /** Reads raw query parameters; blank means absent, the text is folded to lower case. Empty when one part is not valid. */
    public static Optional<CatalogFilter> parse(String text, String countryCode, String language) {
        try {
            return Optional.of(new CatalogFilter(
                    blank(text) ? null : text.strip().toLowerCase(Locale.ROOT),
                    blank(countryCode) ? null : countryCode.strip().toUpperCase(Locale.ROOT),
                    blank(language) ? null : language.strip()));
        } catch (IllegalArgumentException invalid) {
            return Optional.empty();
        }
    }

    public boolean isEmpty() {
        return text == null && countryCode == null && language == null;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
