package com.brandonkamga.sourcefolio.checkup.infra.config;

import com.brandonkamga.sourcefolio.checkup.application.SiteReachability;
import com.brandonkamga.sourcefolio.checkup.application.SiteReachabilityImpl;
import com.brandonkamga.sourcefolio.checkup.domain.port.SiteFetcher;
import com.brandonkamga.sourcefolio.checkup.infra.http.HttpSiteFetcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class CheckupServices {

    // Not wrapped by TransactionalServices: it only makes a network call and touches no table.
    @Bean
    SiteReachability siteReachability(SiteFetcher fetcher) {
        return new SiteReachabilityImpl(fetcher);
    }

    @Bean
    SiteFetcher siteFetcher(@Value("${app.site.url}") String siteUrl) {
        return new HttpSiteFetcher("SourceFolio (+" + siteUrl + ")");
    }
}
