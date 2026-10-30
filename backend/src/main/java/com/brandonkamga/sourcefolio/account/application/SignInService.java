package com.brandonkamga.sourcefolio.account.application;

import com.brandonkamga.sourcefolio.account.domain.model.UserId;

public interface SignInService {

    /** Finds the member behind this provider account, or registers them on their first visit. */
    Member signIn(ExternalIdentity identity);

    Member requireMember(UserId id);
}
