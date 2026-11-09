package com.brandonkamga.sourcefolio.contribution.application;

import com.brandonkamga.sourcefolio.contribution.domain.model.ContributionRecord;
import com.brandonkamga.sourcefolio.contribution.domain.model.ContributionSnapshot;
import com.brandonkamga.sourcefolio.contribution.domain.model.GitHubActivity;
import com.brandonkamga.sourcefolio.contribution.domain.model.ExternalProject;
import com.brandonkamga.sourcefolio.contribution.domain.model.LanguageBytes;
import com.brandonkamga.sourcefolio.contribution.domain.model.MonthActivity;
import com.brandonkamga.sourcefolio.contribution.domain.port.ActivitySource;
import com.brandonkamga.sourcefolio.contribution.domain.port.AnalysisScheduler;
import com.brandonkamga.sourcefolio.contribution.domain.port.ContributionStore;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ContributionAnalysisImpl implements ContributionAnalysis {

    static final Duration WEEKLY = Duration.ofDays(7);
    // Filtering by language looks at the member's main languages, not every file they touched.
    private static final int LANGUAGES_RANKED = 5;

    private final ContributionStore records;
    private final ActivitySource github;
    private final AnalysisScheduler scheduler;
    private final ContributionEvents events;
    private final Clock clock;

    public ContributionAnalysisImpl(ContributionStore records, ActivitySource github, AnalysisScheduler scheduler,
                                    ContributionEvents events, Clock clock) {
        this.records = records;
        this.github = github;
        this.scheduler = scheduler;
        this.events = events;
        this.clock = clock;
    }

    @Override
    public void requestAnalysis(UUID memberId, String login) {
        Instant now = clock.instant();
        Optional<ContributionRecord> known = records.find(memberId);
        if (known.isPresent() && !known.get().worthReading(now)) {
            if (!known.get().login().equals(login)) {
                announce(records.save(known.get().renamed(login)));
            }
            return;
        }
        records.save(known.map(record -> record.askedAgain(login, now)).orElseGet(() -> ContributionRecord.requested(memberId, login, now)));
        scheduler.scheduleAnalysis(memberId);
    }

    @Override
    public void analyze(UUID memberId) {
        ContributionRecord record = records.find(memberId).orElse(null);
        if (record == null) {
            return;
        }
        Optional<GitHubActivity> activity = github.read(record.login(), clock.instant());
        // Read the record again: a sign-in during the call may have renamed the login.
        ContributionRecord current = records.find(memberId).orElse(record);
        Instant now = clock.instant();
        announce(records.save(activity.map(read -> current.analyzed(ContributionSnapshot.of(read, now))).orElseGet(() -> current.failed(now))));
    }

    // The rankings keep a copy of the indicators; a record without a snapshot has none to give.
    private void announce(ContributionRecord record) {
        record.latest().ifPresent(snapshot -> events.analyzed(new ContributionAnalyzed(record.memberId(), record.login(),
                snapshot.name(), snapshot.analyzedAt(), snapshot.accountCreatedAt(), snapshot.publicContributions(),
                snapshot.activeMonths(), snapshot.months().stream().map(MonthActivity::contributions).toList(), snapshot.stars(),
                snapshot.followers(), snapshot.mergedPullRequestsElsewhere(),
                snapshot.languages().stream().map(LanguageBytes::name).limit(LANGUAGES_RANKED).toList(),
                snapshot.externalProjects().stream().map(ExternalProject::nameWithOwner).toList())));
    }

    // Marking each row as asked for before queueing keeps the next sweep from picking it again
    // while its job waits.
    @Override
    public int scheduleDue(int limit) {
        Instant now = clock.instant();
        List<UUID> due = records.findDue(now.minus(WEEKLY), limit);
        due.forEach(memberId -> records.find(memberId).ifPresent(record -> {
            records.save(record.askedAgain(record.login(), now));
            scheduler.scheduleAnalysis(memberId);
        }));
        return due.size();
    }
}
