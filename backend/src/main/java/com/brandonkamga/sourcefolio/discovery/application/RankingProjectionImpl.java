package com.brandonkamga.sourcefolio.discovery.application;

import com.brandonkamga.sourcefolio.contribution.application.ContributionAnalyzed;
import com.brandonkamga.sourcefolio.discovery.domain.port.RankingStore;
import com.brandonkamga.sourcefolio.profile.application.ProfileChanged;

public class RankingProjectionImpl implements RankingProjection {

    private final RankingStore store;

    public RankingProjectionImpl(RankingStore store) {
        this.store = store;
    }

    @Override
    public void onAnalyzed(ContributionAnalyzed event) {
        store.saveIndicators(event.memberId(), event.login(), event.name(), event.analyzedAt(), event.accountCreatedAt(),
                event.publicContributions(), event.activeMonths(), event.months(), event.stars(), event.followers(),
                event.mergedPullRequestsElsewhere(), event.languages());
    }

    // Only what the rankings show or filter on; bio and LinkedIn stay on the profile page.
    @Override
    public void onProfileChanged(ProfileChanged event) {
        store.saveProfile(event.memberId(), event.countryCode(), event.city(), event.published());
    }
}
