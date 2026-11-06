package com.brandonkamga.sourcefolio.contribution.domain.port;

import java.util.UUID;

public interface AnalysisScheduler {

    void scheduleAnalysis(UUID memberId);
}
