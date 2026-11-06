package com.brandonkamga.sourcefolio.contribution.application;

import com.brandonkamga.sourcefolio.contribution.domain.port.ContributionStore;
import java.util.Optional;
import java.util.UUID;

public class ContributionReportsImpl implements ContributionReports {

    private final ContributionStore records;

    public ContributionReportsImpl(ContributionStore records) {
        this.records = records;
    }

    @Override
    public Optional<MyAnalysis> findMine(UUID memberId) {
        return records.find(memberId)
                .map(record -> new MyAnalysis(record.status(), record.refreshing(), record.login(), record.latest().orElse(null)));
    }
}
