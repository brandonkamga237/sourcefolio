package com.brandonkamga.sourcefolio.engagement.application;

import java.util.UUID;

/** Published when a showcase's like count changes. The Ranking is built from it. */
public record ShowcaseLikesChanged(UUID showcaseId, int count) {
}
