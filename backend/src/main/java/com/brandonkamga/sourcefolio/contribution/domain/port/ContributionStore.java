package com.brandonkamga.sourcefolio.contribution.domain.port;

import com.brandonkamga.sourcefolio.contribution.domain.model.ContributionRecord;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContributionStore {

    Optional<ContributionRecord> find(UUID memberId);

    ContributionRecord save(ContributionRecord record);

    /** Members whose latest reading and latest request are both older than the instant, oldest first. */
    List<UUID> findDue(Instant before, int limit);
}
