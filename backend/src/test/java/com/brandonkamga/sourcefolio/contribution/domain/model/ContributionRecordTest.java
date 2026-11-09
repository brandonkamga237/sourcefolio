package com.brandonkamga.sourcefolio.contribution.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContributionRecordTest {

    private static final UUID MEMBER = UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11");
    private static final Instant NOW = Instant.parse("2026-11-06T10:00:00Z");
    // Inside the window of the latest request in every test below.
    private static final Instant AT = NOW.plusSeconds(7230);

    private static ContributionSnapshot snapshotAt(Instant at) {
        return ContributionSnapshot.of(new GitHubActivity(at, null, null, null, 0, 0, 0, List.of(), List.of(), 0, List.of()), at);
    }

    @Test
    @DisplayName("a request without an answer is pending and refreshing")
    void pending() {
        ContributionRecord record = ContributionRecord.requested(MEMBER, "nkeng-dev", NOW);

        assertThat(record.status(NOW.plusSeconds(60))).isEqualTo(AnalysisStatus.PENDING);
        assertThat(record.refreshing(NOW.plusSeconds(60))).isTrue();
        assertThat(record.worthReading(NOW.plusSeconds(30))).isFalse();
        assertThat(record.worthReading(NOW.plusSeconds(180))).isTrue();
    }

    @Test
    @DisplayName("a reading that never comes back ends the wait, and asking again starts a new one")
    void givenUp() {
        ContributionRecord record = ContributionRecord.requested(MEMBER, "nkeng-dev", NOW);
        Instant later = NOW.plus(ContributionRecord.GIVEN_UP_AFTER).plusSeconds(1);

        assertThat(record.status(later)).isEqualTo(AnalysisStatus.FAILED);
        assertThat(record.refreshing(later)).isFalse();
        assertThat(record.worthReading(later)).isTrue();

        ContributionRecord retried = record.askedAgain("nkeng-dev", later);
        assertThat(retried.status(later.plusSeconds(10))).isEqualTo(AnalysisStatus.PENDING);
        assertThat(retried.refreshing(later.plusSeconds(10))).isTrue();
    }

    @Test
    @DisplayName("a reading makes the record ready; it is not read again within the hour")
    void ready() {
        ContributionRecord record = ContributionRecord.requested(MEMBER, "nkeng-dev", NOW).analyzed(snapshotAt(NOW.plusSeconds(5)));

        assertThat(record.status(AT)).isEqualTo(AnalysisStatus.READY);
        assertThat(record.refreshing(AT)).isFalse();
        assertThat(record.worthReading(NOW.plusSeconds(1800))).isFalse();
        assertThat(record.worthReading(NOW.plusSeconds(3700))).isTrue();
    }

    @Test
    @DisplayName("an unknown login fails the first reading but never wipes an earlier snapshot")
    void failure() {
        ContributionRecord first = ContributionRecord.requested(MEMBER, "ghost", NOW).failed(NOW.plusSeconds(3));
        assertThat(first.status(AT)).isEqualTo(AnalysisStatus.FAILED);

        ContributionRecord later = ContributionRecord.requested(MEMBER, "nkeng-dev", NOW).analyzed(snapshotAt(NOW))
                .askedAgain("nkeng-dev", NOW.plusSeconds(7200)).failed(NOW.plusSeconds(7210));
        assertThat(later.status(AT)).isEqualTo(AnalysisStatus.READY);
        assertThat(later.latest()).isPresent();
        assertThat(later.refreshing(AT)).isFalse();
    }

    @Test
    @DisplayName("asking again keeps the old snapshot visible while the new reading is queued")
    void askedAgain() {
        ContributionRecord record = ContributionRecord.requested(MEMBER, "old-login", NOW).analyzed(snapshotAt(NOW))
                .askedAgain("new-login", NOW.plusSeconds(7200));

        assertThat(record.login()).isEqualTo("new-login");
        assertThat(record.status(AT)).isEqualTo(AnalysisStatus.READY);
        assertThat(record.refreshing(AT)).isTrue();
    }
}
