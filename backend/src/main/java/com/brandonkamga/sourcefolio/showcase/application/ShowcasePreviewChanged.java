package com.brandonkamga.sourcefolio.showcase.application;

import java.util.UUID;

/** Published when the author changes their preview; the URLs are null when there is none. */
public record ShowcasePreviewChanged(UUID showcaseId, String imageUrl, String videoUrl) {
}
