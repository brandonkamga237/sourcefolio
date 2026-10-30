package com.brandonkamga.sourcefolio.showcase.application;

import java.util.List;
import java.util.UUID;

/** A showcase is gone for good, with the keys of the preview files left to delete from storage. */
public record ShowcaseRemoved(UUID showcaseId, List<String> mediaKeys) {

    public ShowcaseRemoved {
        mediaKeys = List.copyOf(mediaKeys);
    }
}
