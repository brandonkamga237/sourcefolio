package com.brandonkamga.sourcefolio.showcase.application;

import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseId;
import com.brandonkamga.sourcefolio.showcase.domain.port.RepositoryHost;
import com.brandonkamga.sourcefolio.showcase.domain.port.ShowcaseStore;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;

public class RepositoryStarsImpl implements RepositoryStars {

    private static final Duration FRESH_FOR = Duration.ofHours(24);

    private final ShowcaseStore showcases;
    private final RepositoryHost repositories;
    private final ShowcaseEvents events;
    private final Clock clock;

    public RepositoryStarsImpl(ShowcaseStore showcases, RepositoryHost repositories, ShowcaseEvents events, Clock clock) {
        this.showcases = showcases;
        this.repositories = repositories;
        this.events = events;
        this.clock = clock;
    }

    @Override
    public List<UUID> listDue(int limit) {
        return showcases.findDueForStars(clock.instant().minus(FRESH_FOR), limit).stream().map(ShowcaseId::value).toList();
    }

    // A repository gone private or deleted loses its count rather than showing a stale one.
    @Override
    public void refresh(UUID showcaseId) {
        Showcase showcase = showcases.findById(new ShowcaseId(showcaseId)).orElse(null);
        if (showcase == null) {
            return;
        }
        OptionalInt read = repositories.stars(showcase.sourceRepo().coordinates());
        Integer stars = read.isPresent() ? read.getAsInt() : null;
        Instant now = clock.instant();
        showcases.saveRepositoryStars(showcase.id(), stars, now);
        if (!Objects.equals(stars, showcase.sourceRepo().stars())) {
            events.starsChanged(new ShowcaseStarsChanged(showcaseId, stars));
        }
    }
}
