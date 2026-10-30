package com.brandonkamga.sourcefolio.engagement.application;

/**
 * A showcase's like count, whether the reader likes it (false for a visitor), and whether the
 * reader is its author: the page then shows the count without a button they could press.
 */
public record LikeState(int count, boolean liked, boolean own) {
}
