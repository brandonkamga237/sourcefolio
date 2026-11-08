package com.brandonkamga.sourcefolio.showcase.application;

import java.util.List;
import java.util.UUID;

/** A member is going, with the keys of their preview files left to delete from storage. */
public record ShowcaseRemoved(UUID memberId, List<String> mediaKeys) {

    public ShowcaseRemoved {
        mediaKeys = List.copyOf(mediaKeys);
    }
}
