package com.brandonkamga.sourcefolio.engagement.application;

import com.brandonkamga.sourcefolio.engagement.domain.model.Feedback;
import com.brandonkamga.sourcefolio.engagement.domain.model.FeedbackCursor;
import com.brandonkamga.sourcefolio.engagement.domain.port.FeedbackStore;
import java.util.List;
import java.util.UUID;

/** Reads one page of a showcase's feedback, newest first: shared by its page and its author's inbox. */
final class FeedbackPages {

    static final int MAX_SIZE = 50;

    private FeedbackPages() {
    }

    // One more than asked, to know whether a next page exists without counting.
    static FeedbackPage read(FeedbackStore feedback, UUID showcaseId, String cursor, int size) {
        int limit = Math.clamp(size, 1, MAX_SIZE);
        List<Feedback> rows = feedback.page(showcaseId, FeedbackCursor.decode(cursor).orElse(null), limit + 1);
        int total = feedback.count(showcaseId);
        if (rows.size() <= limit) {
            return new FeedbackPage(rows, null, total);
        }
        List<Feedback> page = rows.subList(0, limit);
        Feedback last = page.getLast();
        return new FeedbackPage(page, new FeedbackCursor(last.createdAt(), last.id()).encode(), total);
    }
}
