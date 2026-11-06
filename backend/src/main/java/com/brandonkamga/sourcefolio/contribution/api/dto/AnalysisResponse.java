package com.brandonkamga.sourcefolio.contribution.api.dto;

import com.brandonkamga.sourcefolio.contribution.application.MyAnalysis;
import com.brandonkamga.sourcefolio.contribution.domain.model.ContributionSnapshot;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * A member's own analysis. status is NONE before the first request; indicators stay null until
 * the first reading. Every figure comes with the GitHub page that proves it.
 */
public record AnalysisResponse(String status, boolean refreshing, String login, Indicators indicators) {

    public static AnalysisResponse none() {
        return new AnalysisResponse("NONE", false, null, null);
    }

    public static AnalysisResponse of(MyAnalysis analysis) {
        return new AnalysisResponse(analysis.status().name(), analysis.refreshing(), analysis.login(),
                analysis.snapshot() == null ? null : Indicators.of(analysis.login(), analysis.snapshot()));
    }

    public record Indicators(Instant analyzedAt, String name, String bio, String location,
                             Instant accountCreatedAt, int publicContributions, int activeMonths, List<Month> months,
                             List<Day> calendar, int stars, int followers, List<Language> languages,
                             int mergedPullRequestsElsewhere, List<Project> externalProjects, List<Repository> topRepositories,
                             Proofs proofs) {

        static Indicators of(String login, ContributionSnapshot snapshot) {
            return new Indicators(snapshot.analyzedAt(), snapshot.name(), snapshot.bio(), snapshot.location(),
                    snapshot.accountCreatedAt(), snapshot.publicContributions(), snapshot.activeMonths(),
                    snapshot.months().stream().map(month -> new Month(month.month().toString(), month.contributions())).toList(),
                    snapshot.calendar().stream().map(day -> new Day(day.date(), day.contributions(), day.level())).toList(),
                    snapshot.stars(), snapshot.followers(),
                    snapshot.languages().stream().map(language -> new Language(language.name(), language.color(), language.bytes())).toList(),
                    snapshot.mergedPullRequestsElsewhere(),
                    snapshot.externalProjects().stream().map(project -> new Project(project.nameWithOwner(),
                            "https://github.com/" + project.nameWithOwner() + "/pulls?q=is%3Apr+is%3Amerged+author%3A" + login,
                            project.mergedPullRequests())).toList(),
                    snapshot.topRepositories().stream().map(repository -> new Repository(repository.name(), repository.description(),
                            repository.url(), repository.stars(), repository.primaryLanguage())).toList(),
                    Proofs.of(login));
        }
    }

    /** month is ISO year-month, e.g. 2026-03. */
    public record Month(String month, int contributions) {
    }

    public record Day(LocalDate date, int contributions, int level) {
    }

    public record Language(String name, String color, long bytes) {
    }

    public record Project(String repository, String url, int mergedPullRequests) {
    }

    public record Repository(String name, String description, String url, int stars, String primaryLanguage) {
    }

    /** The GitHub pages a visitor opens to check each figure. */
    public record Proofs(String profile, String repositories, String mergedElsewhere) {

        static Proofs of(String login) {
            return new Proofs("https://github.com/" + login,
                    "https://github.com/" + login + "?tab=repositories&sort=stargazers",
                    "https://github.com/pulls?q=is%3Apr+is%3Amerged+author%3A" + login + "+-user%3A" + login);
        }
    }
}
