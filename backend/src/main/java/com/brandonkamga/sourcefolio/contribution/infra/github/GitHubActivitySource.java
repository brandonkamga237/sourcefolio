package com.brandonkamga.sourcefolio.contribution.infra.github;

import com.brandonkamga.sourcefolio.contribution.domain.model.CalendarDay;
import com.brandonkamga.sourcefolio.contribution.domain.model.ExternalProject;
import com.brandonkamga.sourcefolio.contribution.domain.model.GitHubActivity;
import com.brandonkamga.sourcefolio.contribution.domain.model.LanguageBytes;
import com.brandonkamga.sourcefolio.contribution.domain.model.OwnedRepository;
import com.brandonkamga.sourcefolio.contribution.domain.port.ActivitySource;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.web.client.RestClient;

/**
 * Anti-corruption layer over GitHub's GraphQL API, the only one that gives the contribution
 * calendar. Called with the platform's token (GraphQL refuses anonymous calls), public data only.
 * GitHub's field names stop here.
 */
public class GitHubActivitySource implements ActivitySource {

    // Owned public repositories, most starred first, 100 a page: three pages cover the stars and
    // languages of nearly everyone while keeping the cost of one reading low.
    private static final int MAX_REPOSITORY_PAGES = 3;

    private static final String PROFILE = """
            query($login: String!, $merged: String!) {
              user(login: $login) {
                createdAt name bio location
                followers { totalCount }
                contributionsCollection {
                  restrictedContributionsCount
                  contributionCalendar {
                    totalContributions
                    weeks { contributionDays { date contributionCount contributionLevel } }
                  }
                }
              }
              search(query: $merged, type: ISSUE, first: 100) {
                issueCount
                nodes { ... on PullRequest { repository { nameWithOwner } } }
              }
            }""";

    private static final String REPOSITORIES = """
            query($login: String!, $after: String) {
              user(login: $login) {
                repositories(first: 100, after: $after, ownerAffiliations: OWNER, isFork: false, privacy: PUBLIC,
                             orderBy: {field: STARGAZERS, direction: DESC}) {
                  pageInfo { hasNextPage endCursor }
                  nodes {
                    name description url stargazerCount
                    primaryLanguage { name }
                    languages(first: 10, orderBy: {field: SIZE, direction: DESC}) { edges { size node { name color } } }
                  }
                }
              }
            }""";

    private final RestClient github;

    public GitHubActivitySource(RestClient github) {
        this.github = github;
    }

    @Override
    public Optional<GitHubActivity> read(String login, Instant now) {
        Response<ProfileData> profile = call(PROFILE, Map.of("login", login,
                "merged", "is:pr is:merged author:" + login + " -user:" + login), ProfileType.class);
        if (profile.data() == null || profile.data().user() == null || profile.data().user().contributionsCollection() == null) {
            return Optional.empty();
        }
        User user = profile.data().user();
        List<OwnedRepository> repositories = repositories(login);
        Calendar calendar = user.contributionsCollection().contributionCalendar();
        List<CalendarDay> days = new ArrayList<>();
        listOf(calendar.weeks()).forEach(week -> listOf(week.contributionDays()).forEach(day ->
                days.add(new CalendarDay(LocalDate.parse(day.date()), orZero(day.contributionCount()), level(day.contributionLevel())))));
        Search search = profile.data().search();
        return Optional.of(new GitHubActivity(user.createdAt(), user.name(), blankToNull(user.bio()), blankToNull(user.location()),
                user.followers() == null ? 0 : orZero(user.followers().totalCount()), orZero(calendar.totalContributions()),
                orZero(user.contributionsCollection().restrictedContributionsCount()), days, repositories,
                search == null ? 0 : orZero(search.issueCount()), search == null ? List.of() : projects(search)));
    }

    private List<OwnedRepository> repositories(String login) {
        List<OwnedRepository> repositories = new ArrayList<>();
        String after = null;
        for (int page = 0; page < MAX_REPOSITORY_PAGES; page++) {
            Map<String, Object> variables = new LinkedHashMap<>();
            variables.put("login", login);
            variables.put("after", after);
            Response<RepositoriesData> response = call(REPOSITORIES, variables, RepositoriesType.class);
            if (response.data() == null || response.data().user() == null) {
                break;
            }
            Repositories connection = response.data().user().repositories();
            listOf(connection.nodes()).forEach(node -> repositories.add(node.toDomain()));
            if (connection.pageInfo() == null || !Boolean.TRUE.equals(connection.pageInfo().hasNextPage())) {
                break;
            }
            after = connection.pageInfo().endCursor();
        }
        return repositories;
    }

    // A search result is a pull request in someone else's repository; the first hundred are grouped
    // by repository, the total comes from issueCount.
    private static List<ExternalProject> projects(Search search) {
        Map<String, Integer> merged = new LinkedHashMap<>();
        listOf(search.nodes()).stream()
                .filter(node -> node.repository() != null)
                .forEach(node -> merged.merge(node.repository().nameWithOwner(), 1, Integer::sum));
        return merged.entrySet().stream().map(entry -> new ExternalProject(entry.getKey(), entry.getValue())).toList();
    }

    private static int level(String contributionLevel) {
        return switch (contributionLevel == null ? "NONE" : contributionLevel) {
            case "FIRST_QUARTILE" -> 1;
            case "SECOND_QUARTILE" -> 2;
            case "THIRD_QUARTILE" -> 3;
            case "FOURTH_QUARTILE" -> 4;
            default -> 0;
        };
    }

    private static <T> List<T> listOf(List<T> values) {
        return values == null ? List.of() : values;
    }

    private static int orZero(Integer value) {
        return value == null ? 0 : value;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    // GraphQL answers 200 with an "errors" list; a login GitHub does not know comes back as a null
    // user with a NOT_FOUND error, which is not a failure of the call. Anything else is.
    private <T> Response<T> call(String query, Map<String, Object> variables, Class<? extends Response<T>> type) {
        Response<T> response = github.post().uri("/graphql")
                .body(Map.of("query", query, "variables", variables))
                .retrieve()
                .body(type);
        if (response == null) {
            throw new IllegalStateException("GitHub GraphQL answered an empty body");
        }
        if (response.errors() != null && response.errors().stream().anyMatch(error -> !"NOT_FOUND".equals(error.type()))) {
            throw new IllegalStateException("GitHub GraphQL error: " + response.errors().getFirst().message());
        }
        return response;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Error(String type, String message) {
    }

    interface Response<T> {
        T data();

        List<Error> errors();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ProfileType(ProfileData data, List<Error> errors) implements Response<ProfileData> {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RepositoriesType(RepositoriesData data, List<Error> errors) implements Response<RepositoriesData> {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ProfileData(User user, Search search) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record User(Instant createdAt, String name, String bio, String location, Count followers, Collection contributionsCollection) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Count(Integer totalCount) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Collection(Integer restrictedContributionsCount, Calendar contributionCalendar) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Calendar(Integer totalContributions, List<Week> weeks) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Week(List<Day> contributionDays) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Day(String date, Integer contributionCount, String contributionLevel) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Search(Integer issueCount, List<SearchNode> nodes) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SearchNode(Repo repository) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Repo(String nameWithOwner) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RepositoriesData(RepositoriesOwner user) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RepositoriesOwner(Repositories repositories) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Repositories(PageInfo pageInfo, List<RepositoryNode> nodes) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PageInfo(Boolean hasNextPage, String endCursor) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RepositoryNode(String name, String description, String url, Integer stargazerCount, Named primaryLanguage, Languages languages) {

        OwnedRepository toDomain() {
            List<LanguageBytes> bytes = languages == null ? List.<LanguageBytes>of() : listOf(languages.edges()).stream()
                    .map(edge -> new LanguageBytes(edge.node().name(), edge.node().color(), edge.size() == null ? 0L : edge.size()))
                    .toList();
            return new OwnedRepository(name, blankToNull(description), url, orZero(stargazerCount),
                    primaryLanguage == null ? null : primaryLanguage.name(), bytes);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Named(String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Languages(List<LanguageEdge> edges) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record LanguageEdge(Long size, LanguageNode node) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record LanguageNode(String name, String color) {
    }
}
