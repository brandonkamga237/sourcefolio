package com.brandonkamga.sourcefolio.account.application;

import com.brandonkamga.sourcefolio.account.domain.model.UserId;
import com.brandonkamga.sourcefolio.account.domain.port.UserStore;
import com.brandonkamga.sourcefolio.shared.exception.NotFoundException;

// The spec allows 30 days; doing it at once is simpler and kinder. Other modules clean up on
// the event first, then the row goes and the foreign keys take what is left.
public class AccountDeletionImpl implements AccountDeletion {

    private final UserStore users;
    private final AccountEvents events;

    public AccountDeletionImpl(UserStore users, AccountEvents events) {
        this.users = users;
        this.events = events;
    }

    @Override
    public void delete(UserId memberId) {
        if (users.find(memberId).isEmpty()) {
            throw new NotFoundException("account.member-not-found");
        }
        events.memberDeleting(new MemberDeleting(memberId.value()));
        users.delete(memberId);
    }
}
