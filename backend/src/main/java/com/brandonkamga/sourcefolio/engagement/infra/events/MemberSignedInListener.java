package com.brandonkamga.sourcefolio.engagement.infra.events;

import com.brandonkamga.sourcefolio.account.application.MemberSignedIn;
import com.brandonkamga.sourcefolio.engagement.application.Feedbacks;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** A GitHub rename reaches the feedback a member already left at their next sign-in. */
@Component("engagementMemberSignedInListener")
class MemberSignedInListener {

    private final Feedbacks feedbacks;

    MemberSignedInListener(Feedbacks feedbacks) {
        this.feedbacks = feedbacks;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void on(MemberSignedIn event) {
        feedbacks.followRename(event.memberId(), event.login());
    }
}
