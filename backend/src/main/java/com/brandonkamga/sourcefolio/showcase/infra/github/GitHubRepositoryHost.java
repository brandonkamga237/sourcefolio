package com.brandonkamga.sourcefolio.showcase.infra.github;

import com.brandonkamga.sourcefolio.showcase.domain.model.PublicRepository;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import com.brandonkamga.sourcefolio.showcase.domain.port.RepositoryHost;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

/**
 * Anti-corruption layer over GitHub's REST API: public data only, through the token of the
 * platform (never the member's). GitHub's field names stop here.
 */
public class GitHubRepositoryHost implements RepositoryHost {

    // Sorted by last push, 100 per page: a portfolio is rarely older than the member's 300 most
    // recently pushed repositories, and three calls keep the rate limit in check.
    private static final int MAX_PAGES = 3;
    private static final ParameterizedTypeReference<List<GitHubRepository>> PAGE = new ParameterizedTypeReference<>() {
    };

    private final RestClient github;

    public GitHubRepositoryHost(RestClient github) {
        this.github = github;
    }

    @Override
    public List<PublicRepository> listPublicRepositories(String login) {
        List<PublicRepository> repositories = new ArrayList<>();
        for (int page = 1; page <= MAX_PAGES; page++) {
            ResponseEntity<List<GitHubRepository>> response;
            try {
                response = github.get()
                        .uri("/users/{login}/repos?type=owner&sort=pushed&per_page=100&page={page}", login, page)
                        .retrieve()
                        .toEntity(PAGE);
            } catch (HttpClientErrorException.NotFound gone) {
                return List.of();
            }
            List<GitHubRepository> body = response.getBody() == null ? List.of() : response.getBody();
            body.forEach(repository -> repositories.add(repository.toDomain()));
            if (!hasNextPage(response.getHeaders())) {
                break;
            }
        }
        return repositories;
    }

    // One call; a private or deleted repository answers 404 to an anonymous or app token.
    @Override
    public OptionalInt stars(RepositoryCoordinates repository) {
        try {
            GitHubStars body = github.get().uri("/repos/{owner}/{name}", repository.owner(), repository.name())
                    .retrieve().body(GitHubStars.class);
            return body == null || body.stargazersCount() == null ? OptionalInt.empty() : OptionalInt.of(body.stargazersCount());
        } catch (HttpClientErrorException.NotFound gone) {
            return OptionalInt.empty();
        }
    }

    private static boolean hasNextPage(HttpHeaders headers) {
        String link = headers.getFirst(HttpHeaders.LINK);
        return link != null && link.contains("rel=\"next\"");
    }

    // Boolean, not boolean: Jackson 3 refuses a missing primitive, and a field GitHub stops
    // sending must not fail the whole detection.
    @JsonIgnoreProperties(ignoreUnknown = true)
    record GitHubRepository(String name, Owner owner, String homepage,
                            @JsonProperty("has_pages") Boolean hasPages,
                            @JsonProperty("pushed_at") Instant pushedAt) {

        PublicRepository toDomain() {
            return new PublicRepository(new RepositoryCoordinates(owner.login(), name), homepage, Boolean.TRUE.equals(hasPages), pushedAt);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Owner(String login) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GitHubStars(@JsonProperty("stargazers_count") Integer stargazersCount) {
    }
}
