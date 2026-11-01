package com.brandonkamga.sourcefolio.engagement.application;

import com.brandonkamga.sourcefolio.engagement.domain.model.Feedback;
import java.util.List;

/** One page of a showcase's feedback, newest first; nextCursor is null on the last page. */
public record FeedbackPage(List<Feedback> items, String nextCursor, int total) {

    public FeedbackPage {
        items = List.copyOf(items);
    }
}
