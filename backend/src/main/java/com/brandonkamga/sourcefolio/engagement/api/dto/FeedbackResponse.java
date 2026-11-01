package com.brandonkamga.sourcefolio.engagement.api.dto;

import com.brandonkamga.sourcefolio.engagement.domain.model.Feedback;
import java.time.Instant;
import java.util.UUID;

/** mine tells the reader they wrote it, so the page offers to remove it. */
public record FeedbackResponse(UUID id, String authorLogin, String angle, String body, String language, Instant createdAt,
                               boolean mine) {

    public static FeedbackResponse of(Feedback feedback, UUID readerId) {
        return new FeedbackResponse(feedback.id(), feedback.authorLogin(), feedback.angle() == null ? null : feedback.angle().name(),
                feedback.body().value(), feedback.language().tag(), feedback.createdAt(), feedback.authorId().equals(readerId));
    }
}
