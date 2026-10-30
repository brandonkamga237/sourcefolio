package com.brandonkamga.sourcefolio.account.domain.model;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Where to warn a member before their portfolio is removed. Checked for shape only: the address
 * is optional and never used to identify anyone, so a typo costs the member a warning, nothing
 * more.
 */
public record EmailAddress(String value) {

    private static final int MAX_LENGTH = 320;
    private static final Pattern SHAPE = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

    public EmailAddress {
        Objects.requireNonNull(value, "value");
        if (value.length() > MAX_LENGTH || !SHAPE.matcher(value).matches()) {
            throw new IllegalArgumentException("Not an email address");
        }
    }

    public static Optional<EmailAddress> parse(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String candidate = raw.strip();
        return candidate.length() <= MAX_LENGTH && SHAPE.matcher(candidate).matches()
                ? Optional.of(new EmailAddress(candidate))
                : Optional.empty();
    }
}
