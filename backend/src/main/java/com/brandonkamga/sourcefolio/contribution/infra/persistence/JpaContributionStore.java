package com.brandonkamga.sourcefolio.contribution.infra.persistence;

import com.brandonkamga.sourcefolio.contribution.domain.model.CalendarDay;
import com.brandonkamga.sourcefolio.contribution.domain.model.ContributionRecord;
import com.brandonkamga.sourcefolio.contribution.domain.model.ContributionSnapshot;
import com.brandonkamga.sourcefolio.contribution.domain.model.ExternalProject;
import com.brandonkamga.sourcefolio.contribution.domain.model.LanguageBytes;
import com.brandonkamga.sourcefolio.contribution.domain.model.MonthActivity;
import com.brandonkamga.sourcefolio.contribution.domain.model.RepositoryFact;
import com.brandonkamga.sourcefolio.contribution.domain.port.ContributionStore;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

// Its own transactions: the analysis use case runs outside one, because it calls GitHub.
@Component
class JpaContributionStore implements ContributionStore {

    private final ContributionRecordJpaRepository records;
    private final JsonMapper json;

    JpaContributionStore(ContributionRecordJpaRepository records, JsonMapper json) {
        this.records = records;
        this.json = json;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ContributionRecord> find(UUID memberId) {
        return records.findById(memberId).map(this::toDomain);
    }

    @Override
    @Transactional
    public ContributionRecord save(ContributionRecord record) {
        ContributionRecordEntity row = records.findById(record.memberId())
                .orElseGet(() -> new ContributionRecordEntity(record.memberId()));
        row.update(record.login(), record.requestedAt(), record.failedAt());
        ContributionSnapshot snapshot = record.snapshot();
        if (snapshot == null) {
            row.snapshot(null, null, null, null, null, null, null, null);
        } else {
            row.snapshot(snapshot.analyzedAt(), snapshot.accountCreatedAt(), snapshot.publicContributions(),
                    (short) snapshot.activeMonths(), snapshot.stars(), snapshot.followers(), snapshot.mergedPullRequestsElsewhere(),
                    json.writeValueAsString(new Details(snapshot.name(), snapshot.bio(), snapshot.location(), snapshot.months(),
                            snapshot.calendar(), snapshot.languages(), snapshot.externalProjects(), snapshot.topRepositories())));
        }
        return toDomain(records.save(row));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> findDue(Instant before, int limit) {
        return records.findDue(before, limit);
    }

    private ContributionRecord toDomain(ContributionRecordEntity row) {
        ContributionSnapshot snapshot = null;
        if (row.getAnalyzedAt() != null) {
            Details details = json.readValue(row.getDetails(), Details.class);
            snapshot = new ContributionSnapshot(row.getAnalyzedAt(), row.getAccountCreatedAt(), details.name(), details.bio(),
                    details.location(), row.getPublicContributions(), details.months(), details.calendar(), row.getStars(),
                    row.getFollowers(), details.languages(), row.getMergedElsewhere(), details.externalProjects(),
                    details.topRepositories());
        }
        return new ContributionRecord(row.getMemberId(), row.getLogin(), row.getRequestedAt(), row.getFailedAt(), snapshot);
    }

    // What only the profile shows. Fields may be added; a renamed or removed one needs every stored
    // document rewritten, by a new reading or a migration.
    record Details(String name, String bio, String location, List<MonthActivity> months, List<CalendarDay> calendar,
                   List<LanguageBytes> languages, List<ExternalProject> externalProjects, List<RepositoryFact> topRepositories) {
    }
}
