package com.brandonkamga.sourcefolio.contribution.infra.config;

import com.brandonkamga.sourcefolio.contribution.application.ContributionAnalysis;
import com.brandonkamga.sourcefolio.contribution.application.ContributionAnalysisImpl;
import com.brandonkamga.sourcefolio.contribution.application.ContributionEvents;
import com.brandonkamga.sourcefolio.contribution.application.ContributionReports;
import com.brandonkamga.sourcefolio.contribution.application.ContributionReportsImpl;
import com.brandonkamga.sourcefolio.contribution.application.PublicProfiles;
import com.brandonkamga.sourcefolio.contribution.application.PublicProfilesImpl;
import com.brandonkamga.sourcefolio.contribution.domain.port.ActivitySource;
import com.brandonkamga.sourcefolio.contribution.domain.port.AnalysisScheduler;
import com.brandonkamga.sourcefolio.contribution.domain.port.ContributionStore;
import com.brandonkamga.sourcefolio.contribution.infra.github.GitHubActivitySource;
import com.brandonkamga.sourcefolio.profile.application.ProfileDirectory;
import com.brandonkamga.sourcefolio.shared.config.TransactionalServices;
import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
class ContributionServices {

    // Not wrapped: analyze calls GitHub, and the store opens a transaction around each write.
    @Bean
    ContributionAnalysis contributionAnalysis(ContributionStore records, ActivitySource github, AnalysisScheduler scheduler,
                                              ContributionEvents events, Clock clock) {
        return new ContributionAnalysisImpl(records, github, scheduler, events, clock);
    }

    @Bean
    ContributionReports contributionReports(ContributionStore records, TransactionalServices transactional) {
        return transactional.wrap(ContributionReports.class, new ContributionReportsImpl(records));
    }

    @Bean
    PublicProfiles publicProfiles(ContributionStore records, ProfileDirectory profiles, TransactionalServices transactional) {
        return transactional.wrap(PublicProfiles.class, new PublicProfilesImpl(records, profiles));
    }

    // GraphQL refuses anonymous calls: without the platform token (development), readings fail and
    // the job's log says so, the rest of the site keeps working.
    @Bean
    ActivitySource activitySource(@Value("${app.github.api-url}") String apiUrl, @Value("${app.github.api-token:}") String apiToken) {
        JdkClientHttpRequestFactory requests = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        requests.setReadTimeout(Duration.ofSeconds(20));
        RestClient.Builder github = RestClient.builder()
                .baseUrl(apiUrl)
                .requestFactory(requests)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "SourceFolio");
        if (!apiToken.isBlank()) {
            github.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken);
        }
        return new GitHubActivitySource(github.build());
    }
}
