package com.brandonkamga.sourcefolio.showcase.application;

import com.brandonkamga.sourcefolio.showcase.domain.port.ShowcaseStore;
import java.util.List;
import java.util.UUID;

public class ShowcaseRemovalImpl implements ShowcaseRemoval {

    private final ShowcaseStore showcases;
    private final ShowcaseEvents events;

    public ShowcaseRemovalImpl(ShowcaseStore showcases, ShowcaseEvents events) {
        this.showcases = showcases;
        this.events = events;
    }

    // The files are not deleted here: storage is a network call, made once the deletion is
    // committed (ShowcaseRemoved), so a rollback never leaves a member without their images.
    @Override
    public void removeAllOf(UUID memberId) {
        List<String> keys = showcases.findMediaKeysByOwner(memberId);
        if (!keys.isEmpty()) {
            events.showcaseRemoved(new ShowcaseRemoved(memberId, keys));
        }
    }
}
