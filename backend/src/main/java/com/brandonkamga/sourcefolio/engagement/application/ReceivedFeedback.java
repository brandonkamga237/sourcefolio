package com.brandonkamga.sourcefolio.engagement.application;

import java.time.Instant;

/**
 * The feedback on an author's portfolio, newest first, with when they last read it (null the
 * first time: everything is new) and how much came after.
 */
public record ReceivedFeedback(FeedbackPage page, Instant seenAt, int unread) {
}
