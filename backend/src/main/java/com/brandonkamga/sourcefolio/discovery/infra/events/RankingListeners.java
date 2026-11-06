package com.brandonkamga.sourcefolio.discovery.infra.events;

import com.brandonkamga.sourcefolio.contribution.application.ContributionAnalyzed;
import com.brandonkamga.sourcefolio.discovery.application.RankingProjection;
import com.brandonkamga.sourcefolio.profile.application.ProfileChanged;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Feeds the rankings. After commit, so a rolled-back publication never shows; analyses are
 * announced outside any transaction, hence fallbackExecution. A failure is logged, not thrown:
 * the member's action succeeded, only the rankings' copy is late, and the next event repairs it.
 */
@Component
class RankingListeners {

    private static final Logger log = LoggerFactory.getLogger(RankingListeners.class);

    private final RankingProjection rankings;

    RankingListeners(RankingProjection rankings) {
        this.rankings = rankings;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void on(ContributionAnalyzed event) {
        try {
            rankings.onAnalyzed(event);
        } catch (RuntimeException failure) {
            log.error("Analysis of member {} not copied to the rankings", event.memberId(), failure);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void on(ProfileChanged event) {
        try {
            rankings.onProfileChanged(event);
        } catch (RuntimeException failure) {
            log.error("Profile of member {} not copied to the rankings", event.memberId(), failure);
        }
    }
}
