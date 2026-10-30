package com.brandonkamga.sourcefolio.showcase.domain.port;

import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryProvider;
import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseId;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseSlug;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShowcaseStore {

    /**
     * Saves the showcase and appends its pending events to the history, atomically. Returns the
     * stored copy. A repository already behind another showcase is refused with a conflict.
     */
    Showcase save(Showcase showcase);

    /** The member's most recent showcase, whatever its status. */
    Optional<Showcase> findByOwner(UUID ownerId);

    Optional<Showcase> findById(ShowcaseId id);

    Optional<Showcase> findBySlug(ShowcaseSlug slug);

    /** Showcases still listed whose repository stars were not read since {@code readBefore}, least recent first. */
    List<ShowcaseId> findDueForStars(Instant readBefore, int limit);

    /**
     * Writes the stars just read and when, and nothing else: an author changing their preview at
     * the same moment keeps their change.
     */
    void saveRepositoryStars(ShowcaseId id, Integer stars, Instant checkedAt);

    boolean existsBySlug(ShowcaseSlug slug);

    boolean existsByRepository(RepositoryProvider provider, RepositoryCoordinates repository);

    /**
     * Deletes every showcase of the member, with what hangs on them (repository, history,
     * likes, catalogue card), and returns them as they were.
     */
    List<Showcase> deleteAllByOwner(UUID ownerId);
}
