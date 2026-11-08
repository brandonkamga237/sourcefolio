package com.brandonkamga.sourcefolio.contribution.application;

import com.brandonkamga.sourcefolio.contribution.domain.model.ContributionSnapshot;
import com.brandonkamga.sourcefolio.profile.application.PublicProfile;

/** A published profile page: the latest snapshot and what the member wrote. */
public record ProfileCard(String login, ContributionSnapshot snapshot, PublicProfile profile) {
}
