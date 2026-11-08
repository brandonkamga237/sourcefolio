package com.brandonkamga.sourcefolio.account.application;

import com.brandonkamga.sourcefolio.account.domain.model.UserId;
import com.brandonkamga.sourcefolio.account.domain.port.UserStore;
import com.brandonkamga.sourcefolio.shared.exception.NotFoundException;

// The spec allows 30 days; doing it at once is simpler and kinder. The row goes and the foreign
// keys take everything the member owns.
public class AccountDeletionImpl implements AccountDeletion {

    private final UserStore users;

    public AccountDeletionImpl(UserStore users) {
        this.users = users;
    }

    @Override
    public void delete(UserId memberId) {
        if (users.find(memberId).isEmpty()) {
            throw new NotFoundException("account.member-not-found");
        }
        users.delete(memberId);
    }
}
