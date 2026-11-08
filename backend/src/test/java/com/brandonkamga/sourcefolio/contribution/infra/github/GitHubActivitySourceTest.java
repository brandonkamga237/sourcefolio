package com.brandonkamga.sourcefolio.contribution.infra.github;

import com.brandonkamga.sourcefolio.contribution.domain.model.CalendarDay;
import com.brandonkamga.sourcefolio.contribution.domain.model.ExternalProject;
import com.brandonkamga.sourcefolio.contribution.domain.model.GitHubActivity;
import com.brandonkamga.sourcefolio.contribution.domain.model.LanguageBytes;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GitHubActivitySourceTest {

    private static final Instant NOW = Instant.parse("2026-11-06T10:00:00Z");

    private final RestClient.Builder builder = RestClient.builder().baseUrl("https://api.github.com")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer platform-token");
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final GitHubActivitySource source = new GitHubActivitySource(builder.build());

    private static final String PROFILE = """
            {"data": {
              "user": {"createdAt": "2017-03-14T08:00:00Z", "name": "Nkeng Tabi", "bio": "  ", "location": "Yaoundé",
                       "followers": {"totalCount": 94},
                       "contributionsCollection": {"restrictedContributionsCount": 10, "contributionCalendar": {
                         "totalContributions": 2150,
                         "weeks": [{"contributionDays": [
                           {"date": "2026-11-05", "contributionCount": 3, "contributionLevel": "SECOND_QUARTILE"},
                           {"date": "2026-11-06", "contributionCount": 0, "contributionLevel": "NONE"}]}]}}},
              "search": {"issueCount": 3, "nodes": [
                {"repository": {"nameWithOwner": "payafrica/momo-sdk"}},
                {"repository": {"nameWithOwner": "payafrica/momo-sdk"}},
                {"repository": {"nameWithOwner": "openlabs/php-validator"}}]}}}
            """;

    @Test
    @DisplayName("the GraphQL answers become GitHub activity, repositories followed page by page, with the platform token")
    void readsActivity() {
        server.expect(requestTo("https://api.github.com/graphql"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer platform-token"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("is:pr is:merged author:nkeng-dev -user:nkeng-dev")))
                .andRespond(withSuccess(PROFILE, MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.github.com/graphql"))
                .andRespond(withSuccess("""
                        {"data": {"user": {"repositories": {"pageInfo": {"hasNextPage": true, "endCursor": "Y3Vyc29y"},
                          "nodes": [{"name": "momo-gateway", "description": "Payments", "url": "https://github.com/nkeng-dev/momo-gateway",
                                     "stargazerCount": 500, "primaryLanguage": {"name": "TypeScript"},
                                     "languages": {"edges": [{"size": 700, "node": {"name": "TypeScript", "color": "#3178c6"}}]}}]}}}}
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.github.com/graphql"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Y3Vyc29y")))
                .andRespond(withSuccess("""
                        {"data": {"user": {"repositories": {"pageInfo": {"hasNextPage": false, "endCursor": null},
                          "nodes": [{"name": "dotfiles", "description": "", "url": "https://github.com/nkeng-dev/dotfiles",
                                     "stargazerCount": 0, "primaryLanguage": null, "languages": {"edges": []}}]}}}}
                        """, MediaType.APPLICATION_JSON));

        GitHubActivity activity = source.read("nkeng-dev", NOW).orElseThrow();

        assertThat(activity.accountCreatedAt()).isEqualTo(Instant.parse("2017-03-14T08:00:00Z"));
        assertThat(activity.bio()).isNull();
        assertThat(activity.location()).isEqualTo("Yaoundé");
        assertThat(activity.followers()).isEqualTo(94);
        assertThat(activity.totalContributions()).isEqualTo(2150);
        assertThat(activity.restrictedContributions()).isEqualTo(10);
        assertThat(activity.calendar()).containsExactly(new CalendarDay(LocalDate.parse("2026-11-05"), 3, 2),
                new CalendarDay(LocalDate.parse("2026-11-06"), 0, 0));
        assertThat(activity.repositories()).hasSize(2);
        assertThat(activity.repositories().getFirst().languages()).containsExactly(new LanguageBytes("TypeScript", "#3178c6", 700));
        assertThat(activity.repositories().get(1).description()).isNull();
        assertThat(activity.mergedPullRequestsElsewhere()).isEqualTo(3);
        assertThat(activity.externalProjects()).containsExactly(new ExternalProject("payafrica/momo-sdk", 2),
                new ExternalProject("openlabs/php-validator", 1));
        server.verify();
    }

    @Test
    @DisplayName("a login GitHub does not know is empty, not an error")
    void unknownLogin() {
        server.expect(requestTo("https://api.github.com/graphql"))
                .andRespond(withSuccess("""
                        {"data": {"user": null, "search": {"issueCount": 0, "nodes": []}},
                         "errors": [{"type": "NOT_FOUND", "message": "Could not resolve to a User with the login of 'ghost'."}]}
                        """, MediaType.APPLICATION_JSON));

        assertThat(source.read("ghost", NOW)).isEmpty();
    }

    @Test
    @DisplayName("any other GraphQL error throws, so the job retries")
    void otherErrors() {
        server.expect(requestTo("https://api.github.com/graphql"))
                .andRespond(withSuccess("""
                        {"errors": [{"type": "RATE_LIMITED", "message": "API rate limit exceeded"}]}
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> source.read("nkeng-dev", NOW)).hasMessageContaining("rate limit");
    }
}
