package com.brandonkamga.sourcefolio.engagement.application;

/** A showcase's like count, and whether the reader likes it (false for a visitor). */
public record LikeState(int count, boolean liked) {
}
