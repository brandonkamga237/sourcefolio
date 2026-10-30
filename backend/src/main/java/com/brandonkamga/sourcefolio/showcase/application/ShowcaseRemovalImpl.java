package com.brandonkamga.sourcefolio.showcase.application;

import com.brandonkamga.sourcefolio.shared.exception.NotFoundException;
import com.brandonkamga.sourcefolio.showcase.domain.model.PreviewMedia;
import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;
import com.brandonkamga.sourcefolio.showcase.domain.port.ShowcaseStore;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

public class ShowcaseRemovalImpl implements ShowcaseRemoval {

    private final ShowcaseStore showcases;
    private final ShowcaseEvents events;

    public ShowcaseRemovalImpl(ShowcaseStore showcases, ShowcaseEvents events) {
        this.showcases = showcases;
        this.events = events;
    }

    @Override
    public void withdraw(UUID memberId) {
        if (remove(memberId).isEmpty()) {
            throw new NotFoundException("showcase.none");
        }
    }

    @Override
    public void removeAllOf(UUID memberId) {
        remove(memberId);
    }

    // The files are not deleted here: storage is a network call, made once the removal is
    // committed (ShowcaseRemoved), so a rollback never leaves a showcase without its images.
    private List<Showcase> remove(UUID memberId) {
        List<Showcase> removed = showcases.deleteAllByOwner(memberId);
        removed.forEach(showcase -> events.showcaseRemoved(new ShowcaseRemoved(showcase.id().value(),
                Stream.of(showcase.previewImage(), showcase.previewVideo()).filter(Objects::nonNull).map(PreviewMedia::key).toList())));
        return removed;
    }
}
