package com.brandonkamga.sourcefolio.shared.domain;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/** Checked for shape only: confirming that an address is really reachable takes an email. */
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
