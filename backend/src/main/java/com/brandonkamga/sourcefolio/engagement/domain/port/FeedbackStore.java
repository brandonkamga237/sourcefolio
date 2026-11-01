package com.brandonkamga.sourcefolio.engagement.domain.port;

import com.brandonkamga.sourcefolio.engagement.domain.model.Feedback;
import com.brandonkamga.sourcefolio.engagement.domain.model.FeedbackCursor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeedbackStore {

    void add(Feedback feedback);

    /** A showcase's visible feedback, newest first, after {@code after} (null for the start), at most {@code limit}. */
    List<Feedback> page(UUID showcaseId, FeedbackCursor after, int limit);

    Optional<Feedback> find(UUID id);

    void delete(UUID id);

    /** How many visible pieces of feedback a showcase has. */
    int count(UUID showcaseId);

    /** Follows a GitHub rename on every piece of feedback the member wrote. */
    void renameAuthor(UUID authorId, String login);
}
