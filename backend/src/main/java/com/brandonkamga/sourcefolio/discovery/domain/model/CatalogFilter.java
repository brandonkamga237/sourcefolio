package com.brandonkamga.sourcefolio.discovery.domain.model;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * What a visitor narrows Discover or Recent to: words found in the author's login, site or
 * headline, the author's country, one language of the source repository, the role the author
 * declares. Each part is optional; none means everything. It narrows, never reorders. The
 * language is read from GitHub: what the code is written in. The role is the author's claim.
 */
public record CatalogFilter(String text, String countryCode, String language, String role) {

    public static final int MAX_TEXT = 60;
    private static final Pattern COUNTRY = Pattern.compile("[A-Z]{2}");
    // GitHub's language names: "TypeScript", "C++", "C#", "Jupyter Notebook", "Vim Script"…
    private static final Pattern ROLE = Pattern.compile("[a-z][a-z-]{1,19}");
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
        if (role != null && !ROLE.matcher(role).matches()) {
            throw new IllegalArgumentException("Not a role: " + role);
        }
    }

    public static CatalogFilter none() {
        return new CatalogFilter(null, null, null, null);
    }

    /** Reads raw query parameters; blank means absent, the text is folded to lower case. Empty when one part is not valid. */
    public static Optional<CatalogFilter> parse(String text, String countryCode, String language, String role) {
        try {
            return Optional.of(new CatalogFilter(
                    blank(text) ? null : text.strip().toLowerCase(Locale.ROOT),
                    blank(countryCode) ? null : countryCode.strip().toUpperCase(Locale.ROOT),
                    blank(language) ? null : language.strip(),
                    blank(role) ? null : role.strip()));
        } catch (IllegalArgumentException invalid) {
            return Optional.empty();
        }
    }

    public boolean isEmpty() {
        return text == null && countryCode == null && language == null && role == null;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
