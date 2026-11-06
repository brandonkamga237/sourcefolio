package com.brandonkamga.sourcefolio.contribution.domain.model;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The six indicators of docs/spec-v2.md at one moment, with what the profile needs to show
 * them. Computed from public GitHub data only; nothing a member types enters here.
 */
public record ContributionSnapshot(Instant analyzedAt, Instant accountCreatedAt, String name, String bio, String location,
                                   int publicContributions, List<MonthActivity> months, List<CalendarDay> calendar,
                                   int stars, int followers, List<LanguageBytes> languages,
                                   int mergedPullRequestsElsewhere, List<ExternalProject> externalProjects,
                                   List<RepositoryFact> topRepositories) {

    public static final int MONTHS = 12;
    private static final int LANGUAGES_KEPT = 12;
    private static final int PROJECTS_KEPT = 10;
    private static final int REPOSITORIES_KEPT = 6;

    public ContributionSnapshot {
        months = List.copyOf(months);
        calendar = List.copyOf(calendar);
        languages = List.copyOf(languages);
        externalProjects = List.copyOf(externalProjects);
        topRepositories = List.copyOf(topRepositories);
    }

    public static ContributionSnapshot of(GitHubActivity activity, Instant now) {
        return new ContributionSnapshot(now, activity.accountCreatedAt(), activity.name(), activity.bio(), activity.location(),
                Math.max(0, activity.totalContributions() - activity.restrictedContributions()),
                months(activity.calendar(), YearMonth.from(now.atZone(ZoneOffset.UTC))),
                activity.calendar(),
                activity.repositories().stream().mapToInt(OwnedRepository::stars).sum(),
                activity.followers(),
                languages(activity.repositories()),
                activity.mergedPullRequestsElsewhere(),
                activity.externalProjects().stream()
                        .sorted(Comparator.comparingInt(ExternalProject::mergedPullRequests).reversed()
                                .thenComparing(ExternalProject::nameWithOwner))
                        .limit(PROJECTS_KEPT).toList(),
                activity.repositories().stream()
                        .sorted(Comparator.comparingInt(OwnedRepository::stars).reversed().thenComparing(OwnedRepository::name))
                        .limit(REPOSITORIES_KEPT)
                        .map(repository -> new RepositoryFact(repository.name(), repository.description(), repository.url(),
                                repository.stars(), repository.primaryLanguage()))
                        .toList());
    }

    /** Months with at least one contribution among the last twelve, the current one included. */
    public int activeMonths() {
        return (int) months.stream().filter(MonthActivity::active).count();
    }

    // GitHub's calendar counts the private contributions of a member who chose to show them, day
    // by day, without telling them apart: regularity reads the calendar as GitHub draws it.
    private static List<MonthActivity> months(List<CalendarDay> calendar, YearMonth current) {
        Map<YearMonth, Integer> totals = new LinkedHashMap<>();
        for (int back = MONTHS - 1; back >= 0; back--) {
            totals.put(current.minusMonths(back), 0);
        }
        calendar.forEach(day -> totals.computeIfPresent(YearMonth.from(day.date()), (month, sum) -> sum + day.contributions()));
        List<MonthActivity> months = new ArrayList<>();
        totals.forEach((month, sum) -> months.add(new MonthActivity(month, sum)));
        return months;
    }

    private static List<LanguageBytes> languages(List<OwnedRepository> repositories) {
        Map<String, LanguageBytes> byName = new LinkedHashMap<>();
        repositories.forEach(repository -> repository.languages().forEach(language -> byName.merge(language.name(), language,
                (seen, more) -> new LanguageBytes(seen.name(), seen.color() != null ? seen.color() : more.color(), seen.bytes() + more.bytes()))));
        return byName.values().stream()
                .sorted(Comparator.comparingLong(LanguageBytes::bytes).reversed().thenComparing(LanguageBytes::name))
                .limit(LANGUAGES_KEPT)
                .toList();
    }
}
