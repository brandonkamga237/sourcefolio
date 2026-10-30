package com.brandonkamga.sourcefolio.showcase.application;

import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;
import java.util.Optional;
import java.util.UUID;

public interface ShowcasePublication {

    /**
     * Publishes one of the member's detected candidates once its site answers. Refused when the
     * member already has a showcase, when the repository is not among their candidates or is
     * already published, and when the site does not answer.
     */
    Showcase publish(PublishShowcase command);

    /** The member's showcase, whatever its status; empty before their first publication. */
    Optional<Showcase> findMyShowcase(UUID memberId);
}
