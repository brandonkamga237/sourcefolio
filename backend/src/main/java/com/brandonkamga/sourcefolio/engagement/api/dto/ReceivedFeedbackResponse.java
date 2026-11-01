package com.brandonkamga.sourcefolio.engagement.api.dto;

import com.brandonkamga.sourcefolio.engagement.application.ReceivedFeedback;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** seenAt is when the author last read it, null the first time; what came after is new. */
public record ReceivedFeedbackResponse(List<FeedbackResponse> items, String nextCursor, int total, Instant seenAt, int unread) {

    public static ReceivedFeedbackResponse of(ReceivedFeedback received, UUID authorId) {
        return new ReceivedFeedbackResponse(received.page().items().stream().map(item -> FeedbackResponse.of(item, authorId)).toList(),
                received.page().nextCursor(), received.page().total(), received.seenAt(), received.unread());
    }
}
