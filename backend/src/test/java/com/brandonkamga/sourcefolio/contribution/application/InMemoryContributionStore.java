package com.brandonkamga.sourcefolio.contribution.application;

import com.brandonkamga.sourcefolio.contribution.domain.model.ContributionRecord;
import com.brandonkamga.sourcefolio.contribution.domain.port.ContributionStore;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// Records are immutable, so storing them is copying them: a forgotten save shows in the test.
class InMemoryContributionStore implements ContributionStore {

    final Map<UUID, ContributionRecord> rows = new LinkedHashMap<>();

    @Override
    public Optional<ContributionRecord> find(UUID memberId) {
        return Optional.ofNullable(rows.get(memberId));
    }

    @Override
    public ContributionRecord save(ContributionRecord record) {
        rows.put(record.memberId(), record);
        return record;
    }

    @Override
    public List<UUID> findDue(Instant before, int limit) {
        return rows.values().stream()
                .filter(record -> last(record).isBefore(before))
                .sorted(Comparator.comparing(InMemoryContributionStore::last))
                .limit(limit)
                .map(ContributionRecord::memberId)
                .toList();
    }

    private static Instant last(ContributionRecord record) {
        return record.latest().map(snapshot -> snapshot.analyzedAt().isAfter(record.requestedAt()) ? snapshot.analyzedAt() : record.requestedAt())
                .orElse(record.requestedAt());
    }
}
