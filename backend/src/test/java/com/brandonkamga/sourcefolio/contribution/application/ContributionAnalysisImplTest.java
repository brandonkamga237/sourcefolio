package com.brandonkamga.sourcefolio.contribution.application;

import com.brandonkamga.sourcefolio.contribution.domain.model.AnalysisStatus;
import com.brandonkamga.sourcefolio.contribution.domain.model.ContributionRecord;
import com.brandonkamga.sourcefolio.contribution.domain.model.GitHubActivity;
import com.brandonkamga.sourcefolio.contribution.domain.port.ActivitySource;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContributionAnalysisImplTest {

    private static final UUID MEMBER = UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11");
    private static final Instant NOW = Instant.parse("2026-11-06T10:00:00Z");

    private final InMemoryContributionStore records = new InMemoryContributionStore();
    private final FakeGitHub github = new FakeGitHub();
    private final List<UUID> scheduled = new ArrayList<>();
    private final MutableClock clock = new MutableClock(NOW);
    private final List<ContributionAnalyzed> announced = new ArrayList<>();
    private final ContributionAnalysisImpl analysis = new ContributionAnalysisImpl(records, github, scheduled::add, announced::add, clock);
    private final ContributionReportsImpl reports = new ContributionReportsImpl(records, clock);

    private static GitHubActivity activity(int followers) {
        return new GitHubActivity(Instant.parse("2017-03-14T08:00:00Z"), "Nkeng", null, null, followers, 40, 0,
                List.of(), List.of(), 2, List.of());
    }

    @Test
    @DisplayName("a first sign-in stores a pending request and queues one reading")
    void firstRequest() {
        analysis.requestAnalysis(MEMBER, "nkeng-dev");

        assertThat(scheduled).containsExactly(MEMBER);
        assertThat(reports.findMine(MEMBER)).hasValueSatisfying(mine -> {
            assertThat(mine.status()).isEqualTo(AnalysisStatus.PENDING);
            assertThat(mine.login()).isEqualTo("nkeng-dev");
            assertThat(mine.snapshot()).isNull();
        });
    }

    @Test
    @DisplayName("the reading stores the snapshot of the six indicators")
    void reading() {
        github.answers.put("nkeng-dev", activity(94));
        analysis.requestAnalysis(MEMBER, "nkeng-dev");
        clock.advance(Duration.ofSeconds(4));

        analysis.analyze(MEMBER);

        assertThat(reports.findMine(MEMBER)).hasValueSatisfying(mine -> {
            assertThat(mine.status()).isEqualTo(AnalysisStatus.READY);
            assertThat(mine.refreshing()).isFalse();
            assertThat(mine.snapshot().followers()).isEqualTo(94);
            assertThat(mine.snapshot().analyzedAt()).isEqualTo(NOW.plusSeconds(4));
        });
        assertThat(announced).singleElement().satisfies(event -> {
            assertThat(event.login()).isEqualTo("nkeng-dev");
            assertThat(event.followers()).isEqualTo(94);
            assertThat(event.months()).hasSize(12);
        });
    }

    @Test
    @DisplayName("signing in again within the hour does not read GitHub again, but follows a renamed login")
    void freshReadingIsNotRepeated() {
        github.answers.put("nkeng-dev", activity(94));
        analysis.requestAnalysis(MEMBER, "nkeng-dev");
        analysis.analyze(MEMBER);
        clock.advance(Duration.ofMinutes(20));

        analysis.requestAnalysis(MEMBER, "nkeng");

        assertThat(scheduled).hasSize(1);
        assertThat(records.find(MEMBER)).map(ContributionRecord::login).hasValue("nkeng");
        assertThat(announced).extracting(ContributionAnalyzed::login).containsExactly("nkeng-dev", "nkeng");
    }

    @Test
    @DisplayName("a reading that never answers ends in FAILED instead of waiting forever")
    void readingThatNeverAnswers() {
        analysis.requestAnalysis(MEMBER, "nkeng-dev");

        clock.advance(Duration.ofMinutes(6));

        assertThat(reports.findMine(MEMBER)).map(MyAnalysis::status).hasValue(AnalysisStatus.FAILED);
        assertThat(reports.findMine(MEMBER)).map(MyAnalysis::refreshing).hasValue(false);
    }

    @Test
    @DisplayName("a login GitHub does not know ends in FAILED instead of waiting forever")
    void unknownLogin() {
        analysis.requestAnalysis(MEMBER, "ghost");

        analysis.analyze(MEMBER);

        assertThat(reports.findMine(MEMBER)).map(MyAnalysis::status).hasValue(AnalysisStatus.FAILED);
        assertThat(announced).isEmpty();
    }

    @Test
    @DisplayName("an outage throws so the job retries, and leaves the request pending")
    void outage() {
        github.down = true;
        analysis.requestAnalysis(MEMBER, "nkeng-dev");

        assertThatThrownBy(() -> analysis.analyze(MEMBER)).isInstanceOf(IllegalStateException.class);
        assertThat(reports.findMine(MEMBER)).map(MyAnalysis::status).hasValue(AnalysisStatus.PENDING);
    }

    @Test
    @DisplayName("the weekly sweep queues the overdue members once, then not again while they wait")
    void weeklySweep() {
        github.answers.put("nkeng-dev", activity(94));
        analysis.requestAnalysis(MEMBER, "nkeng-dev");
        analysis.analyze(MEMBER);
        scheduled.clear();

        clock.advance(Duration.ofDays(6));
        assertThat(analysis.scheduleDue(10)).isZero();

        clock.advance(Duration.ofDays(2));
        assertThat(analysis.scheduleDue(10)).isEqualTo(1);
        assertThat(analysis.scheduleDue(10)).isZero();
        assertThat(scheduled).containsExactly(MEMBER);
    }

    @Test
    @DisplayName("a reading for a member without any request does nothing")
    void noRecord() {
        analysis.analyze(MEMBER);

        assertThat(records.rows).isEmpty();
        assertThat(github.calls).isZero();
    }

    static class FakeGitHub implements ActivitySource {

        final Map<String, GitHubActivity> answers = new HashMap<>();
        boolean down;
        int calls;

        @Override
        public Optional<GitHubActivity> read(String login, Instant now) {
            calls++;
            if (down) {
                throw new IllegalStateException("GitHub is down");
            }
            return Optional.ofNullable(answers.get(login));
        }
    }

    static class MutableClock extends Clock {

        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
