package com.brandonkamga.sourcefolio.engagement.domain.model;

import com.brandonkamga.sourcefolio.shared.domain.Language;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** One member's feedback on a showcase, in their language. The angle is optional. */
public record Feedback(UUID id, UUID showcaseId, UUID authorId, String authorLogin, FeedbackAngle angle, FeedbackBody body,
                       Language language, Instant createdAt) {

    public Feedback {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(showcaseId, "showcaseId");
        Objects.requireNonNull(authorId, "authorId");
        Objects.requireNonNull(authorLogin, "authorLogin");
        Objects.requireNonNull(body, "body");
        Objects.requireNonNull(language, "language");
        Objects.requireNonNull(createdAt, "createdAt");
    }

    public static Feedback write(UUID showcaseId, UUID authorId, String authorLogin, FeedbackAngle angle, FeedbackBody body,
                                 Language language, Instant now) {
        return new Feedback(UUID.randomUUID(), showcaseId, authorId, authorLogin, angle, body, language, now);
    }
}
