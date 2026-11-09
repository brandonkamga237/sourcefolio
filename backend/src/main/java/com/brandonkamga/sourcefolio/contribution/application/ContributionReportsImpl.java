package com.brandonkamga.sourcefolio.contribution.application;

import com.brandonkamga.sourcefolio.contribution.domain.port.ContributionStore;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class ContributionReportsImpl implements ContributionReports {

    private final ContributionStore records;
    private final Clock clock;

    public ContributionReportsImpl(ContributionStore records, Clock clock) {
        this.records = records;
        this.clock = clock;
    }

    @Override
    public Optional<MyAnalysis> findMine(UUID memberId) {
        Instant now = clock.instant();
        return records.find(memberId)
                .map(record -> new MyAnalysis(record.status(now), record.refreshing(now), record.login(), record.latest().orElse(null)));
    }
}
