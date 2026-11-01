package com.brandonkamga.sourcefolio.engagement.domain.model;

import java.util.Arrays;
import java.util.Optional;

/** What a piece of feedback looks at (spec), so an author knows where to act. */
public enum FeedbackAngle {
    DESIGN, CLARITY, PERFORMANCE, MOBILE, CODE;

    public static Optional<FeedbackAngle> parse(String raw) {
        return Arrays.stream(values()).filter(angle -> angle.name().equalsIgnoreCase(raw == null ? "" : raw.strip())).findFirst();
    }
}
