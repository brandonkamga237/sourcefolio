package com.brandonkamga.sourcefolio.profile.domain.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * The one role a member says they work in, stored and exchanged as a slug and translated at
 * display time. A claim, shown as theirs: the stack read from GitHub is what the code proves.
 */
public enum Role {
    FRONTEND("frontend"),
    BACKEND("backend"),
    FULLSTACK("fullstack"),
    MOBILE("mobile"),
    DEVOPS("devops"),
    DATA_AI("data-ai"),
    SECURITY("security"),
    DESIGN("design"),
    QA("qa"),
    GAME("game"),
    EMBEDDED("embedded"),
    LEARNING("learning");

    private final String slug;

    Role(String slug) {
        this.slug = slug;
    }

    public String slug() {
        return slug;
    }

    public static Optional<Role> fromSlug(String slug) {
        return Arrays.stream(values()).filter(role -> role.slug.equals(slug)).findFirst();
    }
}
