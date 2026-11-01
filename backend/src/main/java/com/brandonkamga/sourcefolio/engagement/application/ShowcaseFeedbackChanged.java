package com.brandonkamga.sourcefolio.engagement.application;

import java.util.UUID;

/** Published when the amount of feedback a showcase shows changes. Cards display it. */
public record ShowcaseFeedbackChanged(UUID showcaseId, int count) {
}
