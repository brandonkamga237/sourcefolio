package com.brandonkamga.sourcefolio.showcase.domain.port;

import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryProvider;
import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;
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

    boolean existsByRepository(RepositoryProvider provider, RepositoryCoordinates repository);
}
