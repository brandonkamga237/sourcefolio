package com.brandonkamga.sourcefolio.shared.domain;

import java.util.Locale;

/** The two languages the product speaks. Anything else is served in English. */
public enum Language {
    FR,
    EN;

    public static Language of(Locale locale) {
        return locale != null && "fr".equals(locale.getLanguage()) ? FR : EN;
    }

    public String tag() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Language fromTag(String tag) {
        return "fr".equals(tag) ? FR : EN;
    }
}
