package com.brandonkamga.sourcefolio.contribution.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContributionSnapshotTest {

    private static final Instant NOW = Instant.parse("2026-11-06T10:00:00Z");

    static GitHubActivity activity(List<CalendarDay> calendar, List<OwnedRepository> repositories, List<ExternalProject> projects) {
        return new GitHubActivity(Instant.parse("2017-03-14T08:00:00Z"), "Nkeng Tabi", "Backend PHP", "Yaoundé", 94,
                2_150, 10, calendar, repositories, 14, projects);
    }

    @Test
    @DisplayName("private contributions GitHub counts are taken out of the public volume")
    void volumeIsPublicOnly() {
        ContributionSnapshot snapshot = ContributionSnapshot.of(activity(List.of(), List.of(), List.of()), NOW);

        assertThat(snapshot.publicContributions()).isEqualTo(2_140);
        assertThat(snapshot.analyzedAt()).isEqualTo(NOW);
        assertThat(snapshot.followers()).isEqualTo(94);
        assertThat(snapshot.mergedPullRequestsElsewhere()).isEqualTo(14);
    }

    @Test
    @DisplayName("regularity counts the active months among the last twelve, the current one included")
    void regularity() {
        List<CalendarDay> calendar = List.of(
                new CalendarDay(LocalDate.parse("2025-11-03"), 2, 1),
                new CalendarDay(LocalDate.parse("2025-10-31"), 5, 2),
                new CalendarDay(LocalDate.parse("2026-03-10"), 0, 0),
                new CalendarDay(LocalDate.parse("2026-06-01"), 1, 1),
                new CalendarDay(LocalDate.parse("2026-11-05"), 3, 2),
                new CalendarDay(LocalDate.parse("2026-11-06"), 1, 1));

        ContributionSnapshot snapshot = ContributionSnapshot.of(activity(calendar, List.of(), List.of()), NOW);

        assertThat(snapshot.months()).hasSize(12);
        assertThat(snapshot.months().getFirst().month()).isEqualTo(YearMonth.of(2025, 12));
        assertThat(snapshot.months().getLast()).isEqualTo(new MonthActivity(YearMonth.of(2026, 11), 4));
        // October and November 2025 fall outside the twelve months; March has a day without any.
        assertThat(snapshot.activeMonths()).isEqualTo(2);
    }

    @Test
    @DisplayName("stars add up, languages merge across repositories by bytes, and the most starred repositories lead")
    void repositories() {
        List<OwnedRepository> repositories = List.of(
                new OwnedRepository("laravel-fcfa", null, "https://github.com/n/laravel-fcfa", 12, "PHP",
                        List.of(new LanguageBytes("PHP", "#4F5D95", 900), new LanguageBytes("Blade", null, 100))),
                new OwnedRepository("momo-gateway", "Mobile payments", "https://github.com/n/momo-gateway", 500, "TypeScript",
                        List.of(new LanguageBytes("TypeScript", "#3178c6", 700), new LanguageBytes("PHP", "#4F5D95", 300))));

        ContributionSnapshot snapshot = ContributionSnapshot.of(activity(List.of(), repositories, List.of()), NOW);

        assertThat(snapshot.stars()).isEqualTo(512);
        assertThat(snapshot.languages()).extracting(LanguageBytes::name).containsExactly("PHP", "TypeScript", "Blade");
        assertThat(snapshot.languages().getFirst().bytes()).isEqualTo(1_200);
        assertThat(snapshot.topRepositories()).extracting(RepositoryFact::name).containsExactly("momo-gateway", "laravel-fcfa");
    }

    @Test
    @DisplayName("projects elsewhere are ordered by merged pull requests")
    void projectsElsewhere() {
        List<ExternalProject> projects = List.of(new ExternalProject("openlabs/php-validator", 4),
                new ExternalProject("payafrica/momo-sdk", 6), new ExternalProject("devdocs-fr/traductions", 4));

        ContributionSnapshot snapshot = ContributionSnapshot.of(activity(List.of(), List.of(), projects), NOW);

        assertThat(snapshot.externalProjects()).extracting(ExternalProject::nameWithOwner)
                .containsExactly("payafrica/momo-sdk", "devdocs-fr/traductions", "openlabs/php-validator");
    }

    @Test
    @DisplayName("an empty account is a valid snapshot, not an error: zero is a starting point")
    void emptyAccount() {
        GitHubActivity empty = new GitHubActivity(NOW, null, null, null, 0, 0, 0, List.of(), List.of(), 0, List.of());

        ContributionSnapshot snapshot = ContributionSnapshot.of(empty, NOW);

        assertThat(snapshot.activeMonths()).isZero();
        assertThat(snapshot.stars()).isZero();
        assertThat(snapshot.languages()).isEmpty();
    }
}
