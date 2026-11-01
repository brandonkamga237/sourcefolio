package com.brandonkamga.sourcefolio.engagement.infra.events;

import com.brandonkamga.sourcefolio.account.application.MemberDeleting;
import com.brandonkamga.sourcefolio.engagement.application.Feedbacks;
import com.brandonkamga.sourcefolio.engagement.application.Likes;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Inside the deletion's transaction: a failure here must stop the account deletion too. */
@Component("engagementMemberDeletingListener")
class MemberDeletingListener {

    private final Likes likes;
    private final Feedbacks feedbacks;

    MemberDeletingListener(Likes likes, Feedbacks feedbacks) {
        this.likes = likes;
        this.feedbacks = feedbacks;
    }

    @EventListener
    void on(MemberDeleting event) {
        likes.withdrawAllBy(event.memberId());
        feedbacks.withdrawAllBy(event.memberId());
    }
}
