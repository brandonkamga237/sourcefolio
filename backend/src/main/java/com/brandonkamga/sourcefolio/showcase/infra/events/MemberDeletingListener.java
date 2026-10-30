package com.brandonkamga.sourcefolio.showcase.infra.events;

import com.brandonkamga.sourcefolio.account.application.MemberDeleting;
import com.brandonkamga.sourcefolio.showcase.application.ShowcaseRemoval;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Inside the deletion's transaction: a failure here must stop the account deletion too. */
@Component("showcaseMemberDeletingListener")
class MemberDeletingListener {

    private final ShowcaseRemoval removal;

    MemberDeletingListener(ShowcaseRemoval removal) {
        this.removal = removal;
    }

    @EventListener
    void on(MemberDeleting event) {
        removal.removeAllOf(event.memberId());
    }
}
