package com.brandonkamga.sourcefolio.showcase.infra.events;

import com.brandonkamga.sourcefolio.account.application.MemberSignedIn;
import com.brandonkamga.sourcefolio.showcase.application.ShowcaseDetection;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Every sign-in refreshes the member's portfolio candidates. After commit: the job must never
 * run for a member whose sign-in was rolled back, nor before their row is visible.
 */
@Component
class MemberSignedInListener {

    private final ShowcaseDetection detection;

    MemberSignedInListener(ShowcaseDetection detection) {
        this.detection = detection;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void on(MemberSignedIn event) {
        detection.requestDetection(event.memberId(), event.login());
    }
}
