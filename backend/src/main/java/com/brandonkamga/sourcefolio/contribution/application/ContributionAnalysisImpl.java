package com.brandonkamga.sourcefolio.contribution.application;

import com.brandonkamga.sourcefolio.contribution.domain.model.ContributionRecord;
import com.brandonkamga.sourcefolio.contribution.domain.model.ContributionSnapshot;
import com.brandonkamga.sourcefolio.contribution.domain.model.GitHubActivity;
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

    private final ContributionStore records;
    private final ActivitySource github;
    private final AnalysisScheduler scheduler;
    private final Clock clock;

    public ContributionAnalysisImpl(ContributionStore records, ActivitySource github, AnalysisScheduler scheduler, Clock clock) {
        this.records = records;
        this.github = github;
        this.scheduler = scheduler;
        this.clock = clock;
    }

    @Override
    public void requestAnalysis(UUID memberId, String login) {
        Instant now = clock.instant();
        Optional<ContributionRecord> known = records.find(memberId);
        if (known.isPresent() && !known.get().worthReading(now)) {
            if (!known.get().login().equals(login)) {
                records.save(known.get().renamed(login));
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
        records.save(activity.map(read -> current.analyzed(ContributionSnapshot.of(read, now))).orElseGet(() -> current.failed(now)));
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
