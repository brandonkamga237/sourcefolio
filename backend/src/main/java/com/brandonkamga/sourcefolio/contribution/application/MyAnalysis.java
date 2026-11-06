package com.brandonkamga.sourcefolio.contribution.application;

import com.brandonkamga.sourcefolio.contribution.domain.model.AnalysisStatus;
import com.brandonkamga.sourcefolio.contribution.domain.model.ContributionSnapshot;

/** What the member sees of their own analysis; snapshot is null until the first reading. */
public record MyAnalysis(AnalysisStatus status, boolean refreshing, String login, ContributionSnapshot snapshot) {
}
