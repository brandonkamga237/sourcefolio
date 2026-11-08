package com.brandonkamga.sourcefolio.contribution.application;

import java.util.Optional;
import java.util.UUID;

public interface ContributionReports {

    /** Empty when no analysis was ever asked for (a member who signed in before this module). */
    Optional<MyAnalysis> findMine(UUID memberId);
}
