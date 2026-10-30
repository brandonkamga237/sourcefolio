package com.brandonkamga.sourcefolio.showcase.infra.config;

import com.brandonkamga.sourcefolio.showcase.application.ShowcaseDetection;
import com.brandonkamga.sourcefolio.showcase.application.ShowcaseDetectionImpl;
import com.brandonkamga.sourcefolio.showcase.domain.port.CandidateStore;
import com.brandonkamga.sourcefolio.showcase.domain.port.DetectionScheduler;
import com.brandonkamga.sourcefolio.showcase.domain.port.RepositoryHost;
import com.brandonkamga.sourcefolio.showcase.infra.github.GitHubRepositoryHost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;

@Configuration
class ShowcaseServices {

    // Not wrapped by TransactionalServices: detect() calls GitHub, and a network call never runs
    // inside a transaction. The store makes its own write atomic.
    @Bean
    ShowcaseDetection showcaseDetection(RepositoryHost repositories, CandidateStore candidates,
                                        DetectionScheduler scheduler, Clock clock) {
        return new ShowcaseDetectionImpl(repositories, candidates, scheduler, clock);
    }

    @Bean
    RepositoryHost repositoryHost(@Value("${app.github.api-url}") String apiUrl,
                                  @Value("${app.github.api-token:}") String apiToken) {
        JdkClientHttpRequestFactory requests = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        requests.setReadTimeout(Duration.ofSeconds(15));
        RestClient.Builder github = RestClient.builder()
                .baseUrl(apiUrl)
                .requestFactory(requests)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .defaultHeader(HttpHeaders.USER_AGENT, "SourceFolio");
        // Without a token GitHub allows 60 calls an hour per address, too few beyond development.
        if (!apiToken.isBlank()) {
            github.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken);
        }
        return new GitHubRepositoryHost(github.build());
    }
}
