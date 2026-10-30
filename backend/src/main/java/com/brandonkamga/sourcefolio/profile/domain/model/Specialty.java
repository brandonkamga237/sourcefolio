package com.brandonkamga.sourcefolio.profile.domain.model;

import java.util.Arrays;
import java.util.Optional;

/** The specialties of the spec, one per member, stored and exchanged as slugs. */
public enum Specialty {
    FRONTEND("frontend"),
    BACKEND("backend"),
    FULLSTACK("fullstack"),
    MOBILE("mobile"),
    DEVOPS("devops"),
    DATA_AI("data-ai"),
    GAME("game"),
    EMBEDDED("embedded"),
    SECURITY("security"),
    OTHER("other");

    private final String slug;

    Specialty(String slug) {
        this.slug = slug;
    }

    public String slug() {
        return slug;
    }

    public static Optional<Specialty> fromSlug(String slug) {
        return Arrays.stream(values()).filter(specialty -> specialty.slug.equals(slug)).findFirst();
    }
}
