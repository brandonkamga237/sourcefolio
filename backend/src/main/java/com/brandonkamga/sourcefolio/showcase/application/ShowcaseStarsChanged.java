package com.brandonkamga.sourcefolio.showcase.application;

import java.util.UUID;

/** The star count of a showcase's repository moved; null when the repository can no longer be read. */
public record ShowcaseStarsChanged(UUID showcaseId, Integer stars) {
}
