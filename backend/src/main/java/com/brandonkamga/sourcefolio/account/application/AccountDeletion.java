package com.brandonkamga.sourcefolio.account.application;

import com.brandonkamga.sourcefolio.account.domain.model.UserId;

/** Deleting one's own account, at once and for good. */
public interface AccountDeletion {

    void delete(UserId memberId);
}
