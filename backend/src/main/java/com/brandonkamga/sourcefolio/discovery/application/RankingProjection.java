package com.brandonkamga.sourcefolio.discovery.application;

import com.brandonkamga.sourcefolio.contribution.application.ContributionAnalyzed;
import com.brandonkamga.sourcefolio.profile.application.ProfileChanged;

/** Keeps the rankings' copy of what the contribution and profile modules announce. */
public interface RankingProjection {

    void onAnalyzed(ContributionAnalyzed event);

    void onProfileChanged(ProfileChanged event);
}
