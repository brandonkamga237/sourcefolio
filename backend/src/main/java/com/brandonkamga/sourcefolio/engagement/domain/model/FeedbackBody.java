package com.brandonkamga.sourcefolio.engagement.domain.model;

import java.util.Objects;

/**
 * The words of a piece of feedback: 10 to 1,000 characters (spec: useful feedback rather than
 * "Nice!"), trimmed, line breaks kept since a remark may come in points.
 */
public record FeedbackBody(String value) {

    public static final int MIN = 10;
    public static final int MAX = 1_000;

    public FeedbackBody {
        Objects.requireNonNull(value, "value");
        if (value.length() < MIN || value.length() > MAX || !value.equals(value.strip())) {
            throw new IllegalArgumentException("Not a feedback body of " + MIN + " to " + MAX + " characters");
        }
    }

    public static FeedbackBody parse(String raw) {
        return new FeedbackBody(raw == null ? "" : raw.replace("\r\n", "\n").strip());
    }
}
