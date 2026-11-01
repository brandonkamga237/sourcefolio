package com.brandonkamga.sourcefolio.engagement.application;

import java.util.UUID;

/** What an author sees of the feedback their portfolio receives (spec: "Retours reçus"). */
public interface FeedbackInbox {

    /** Empty before their first publication. */
    ReceivedFeedback findReceived(UUID memberId, String cursor, int size);

    /** Feedback written since they last read it: the number on their Me tab. */
    int countUnread(UUID memberId);

    /** They have read what is there now. */
    void markSeen(UUID memberId);
}
