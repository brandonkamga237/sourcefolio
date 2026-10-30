package com.brandonkamga.sourcefolio.showcase.infra.config;

import com.brandonkamga.sourcefolio.checkup.application.SiteReachability;
import com.brandonkamga.sourcefolio.shared.config.TransactionalServices;
import com.brandonkamga.sourcefolio.showcase.application.PublicShowcases;
import com.brandonkamga.sourcefolio.showcase.application.PublicShowcasesImpl;
import com.brandonkamga.sourcefolio.showcase.application.ShowcaseDetection;
import com.brandonkamga.sourcefolio.showcase.application.ShowcaseDetectionImpl;
import com.brandonkamga.sourcefolio.showcase.application.ShowcaseEvents;
import com.brandonkamga.sourcefolio.showcase.application.ShowcasePreview;
import com.brandonkamga.sourcefolio.showcase.application.ShowcasePreviewImpl;
import com.brandonkamga.sourcefolio.showcase.application.ShowcasePublication;
import com.brandonkamga.sourcefolio.showcase.application.ShowcasePublicationImpl;
import com.brandonkamga.sourcefolio.showcase.application.ShowcaseRemoval;
import com.brandonkamga.sourcefolio.showcase.application.ShowcaseRemovalImpl;
import com.brandonkamga.sourcefolio.showcase.domain.port.CandidateStore;
import com.brandonkamga.sourcefolio.showcase.domain.port.DetectionScheduler;
import com.brandonkamga.sourcefolio.showcase.domain.port.MemberDirectory;
import com.brandonkamga.sourcefolio.showcase.domain.port.RepositoryHost;
import com.brandonkamga.sourcefolio.showcase.domain.port.ShowcaseStore;
import com.brandonkamga.sourcefolio.storage.application.MediaStore;
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

    // Not wrapped either: publish() fetches the member's site before saving, and the store
    // makes the save atomic.
    @Bean
    ShowcasePublication showcasePublication(ShowcaseStore showcases, CandidateStore candidates,
                                            SiteReachability sites, MemberDirectory members, ShowcaseEvents events,
                                            Clock clock) {
        return new ShowcasePublicationImpl(showcases, candidates, sites, members, events, clock);
    }

    // Not wrapped: it uploads to the bucket before saving; the store makes the save atomic.
    @Bean
    ShowcasePreview showcasePreview(ShowcaseStore showcases, MediaStore media, ShowcaseEvents events) {
        return new ShowcasePreviewImpl(showcases, media, events);
    }

    @Bean
    ShowcaseRemoval showcaseRemoval(ShowcaseStore showcases, ShowcaseEvents events, TransactionalServices transactional) {
        return transactional.wrap(ShowcaseRemoval.class, new ShowcaseRemovalImpl(showcases, events));
    }

    @Bean
    PublicShowcases publicShowcases(ShowcaseStore showcases, MemberDirectory members, TransactionalServices transactional) {
        return transactional.wrap(PublicShowcases.class, new PublicShowcasesImpl(showcases, members));
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
