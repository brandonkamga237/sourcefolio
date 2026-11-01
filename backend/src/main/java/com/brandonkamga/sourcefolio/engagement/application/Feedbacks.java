package com.brandonkamga.sourcefolio.engagement.application;

import com.brandonkamga.sourcefolio.engagement.domain.model.Feedback;
import com.brandonkamga.sourcefolio.shared.domain.Language;
import java.util.UUID;

/** Guided feedback on showcases (spec): anyone reads it, members write it, each removes their own. */
public interface Feedbacks {

    int MAX_PAGE_SIZE = 50;

    FeedbackPage list(String slug, String cursor, int size);

    /** An optional angle and 10 to 1,000 characters, in the member's language; never on one's own showcase. */
    Feedback post(UUID memberId, String slug, String angle, String body, Language language);

    /** Only the one who wrote it removes it. */
    void delete(UUID memberId, UUID feedbackId);

    /** Keeps the author's login on their feedback in step with GitHub. */
    void followRename(UUID memberId, String login);

    /** A member leaving takes their feedback with them; each showcase's new count is announced. */
    void withdrawAllBy(UUID memberId);
}
