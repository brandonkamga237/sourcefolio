package com.brandonkamga.sourcefolio.showcase.infra.github;

import com.brandonkamga.sourcefolio.showcase.domain.model.PublicRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GitHubRepositoryHostTest {

    private final RestClient.Builder builder = RestClient.builder().baseUrl("https://api.github.com")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer platform-token");
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final GitHubRepositoryHost host = new GitHubRepositoryHost(builder.build());

    @Test
    @DisplayName("GitHub's repository JSON becomes the fields detection needs, with the platform token")
    void mapsRepositories() {
        server.expect(requestTo("https://api.github.com/users/nekwalla/repos?type=owner&sort=pushed&per_page=100&page=1"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer platform-token"))
                .andRespond(withSuccess("""
                        [{"name": "nekwalla.github.io", "owner": {"login": "nekwalla"}, "homepage": null,
                          "has_pages": true, "pushed_at": "2026-10-29T23:03:39Z", "fork": false, "stargazers_count": 3},
                         {"name": "api", "owner": {"login": "nekwalla"}, "homepage": "https://api.example.org",
                          "has_pages": false, "pushed_at": null}]
                        """, MediaType.APPLICATION_JSON));

        List<PublicRepository> repositories = host.listPublicRepositories("nekwalla");

        assertThat(repositories).hasSize(2);
        assertThat(repositories.getFirst()).satisfies(repository -> {
            assertThat(repository.coordinates().toString()).isEqualTo("nekwalla/nekwalla.github.io");
            assertThat(repository.hasPages()).isTrue();
            assertThat(repository.pushedAt()).isEqualTo(Instant.parse("2026-10-29T23:03:39Z"));
        });
        assertThat(repositories.get(1).homepage()).isEqualTo("https://api.example.org");
        server.verify();
    }

    @Test
    @DisplayName("pages are followed through the Link header")
    void followsPages() {
        server.expect(requestTo("https://api.github.com/users/nekwalla/repos?type=owner&sort=pushed&per_page=100&page=1"))
                .andRespond(withSuccess("[{\"name\": \"a\", \"owner\": {\"login\": \"nekwalla\"}}]", MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.LINK, "<https://api.github.com/user/1/repos?page=2>; rel=\"next\", <https://api.github.com/user/1/repos?page=2>; rel=\"last\""));
        server.expect(requestTo("https://api.github.com/users/nekwalla/repos?type=owner&sort=pushed&per_page=100&page=2"))
                .andRespond(withSuccess("[{\"name\": \"b\", \"owner\": {\"login\": \"nekwalla\"}}]", MediaType.APPLICATION_JSON));

        assertThat(host.listPublicRepositories("nekwalla")).extracting(r -> r.coordinates().name()).containsExactly("a", "b");
        server.verify();
    }

    @Test
    @DisplayName("an account that no longer exists has no repositories, it is not an error")
    void unknownAccountIsEmpty() {
        server.expect(requestTo("https://api.github.com/users/gone/repos?type=owner&sort=pushed&per_page=100&page=1"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(host.listPublicRepositories("gone")).isEmpty();
    }
}
