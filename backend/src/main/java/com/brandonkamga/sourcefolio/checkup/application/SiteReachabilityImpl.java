package com.brandonkamga.sourcefolio.checkup.application;

import com.brandonkamga.sourcefolio.checkup.domain.port.SiteFetcher;

public class SiteReachabilityImpl implements SiteReachability {

    private final SiteFetcher fetcher;

    public SiteReachabilityImpl(SiteFetcher fetcher) {
        this.fetcher = fetcher;
    }

    // A 4xx or 5xx means a visitor sees an error page, so it does not count as answering.
    @Override
    public boolean isReachable(String url) {
        return fetcher.statusOf(url).stream().anyMatch(status -> status >= 200 && status < 300);
    }
}
