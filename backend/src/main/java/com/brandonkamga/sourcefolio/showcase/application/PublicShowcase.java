package com.brandonkamga.sourcefolio.showcase.application;

import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;

/** A showcase as a visitor sees it, with its author's current GitHub login. */
public record PublicShowcase(Showcase showcase, String ownerLogin) {
}
