package com.brandonkamga.sourcefolio.account.domain.model;

import com.brandonkamga.sourcefolio.shared.domain.Language;

import java.time.Instant;
import java.util.Objects;

/**
 * A member of the platform, independent of how they sign in. Identified by id, never by email or
 * GitHub login: logins change, and the email is optional (asked only when publishing).
 */
public record User(UserId id, Language language, String email, Instant createdAt) {

    public User {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(language, "language");
        Objects.requireNonNull(createdAt, "createdAt");
    }

    public static User register(Language language, Instant now) {
        return new User(UserId.newId(), language, null, now);
    }

    /** The same member with a new address, or none. */
    public User withEmail(EmailAddress email) {
        return new User(id, language, email == null ? null : email.value(), createdAt);
    }
}
