package com.brandonkamga.sourcefolio.contribution.infra.events;

import com.brandonkamga.sourcefolio.account.application.MemberSignedIn;
import com.brandonkamga.sourcefolio.contribution.application.ContributionAnalysis;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Every sign-in asks for a reading; the use case skips it when the last one is fresh. */
@Component("contributionMemberSignedInListener")
class MemberSignedInListener {

    private final ContributionAnalysis analysis;

    MemberSignedInListener(ContributionAnalysis analysis) {
        this.analysis = analysis;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void on(MemberSignedIn event) {
        analysis.requestAnalysis(event.memberId(), event.login());
    }
}
