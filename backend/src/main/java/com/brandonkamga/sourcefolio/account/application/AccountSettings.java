package com.brandonkamga.sourcefolio.account.application;

import com.brandonkamga.sourcefolio.account.domain.model.UserId;
import java.util.Optional;

/** What a member sets about their account. Only the alert address so far. */
public interface AccountSettings {

    Optional<String> findEmail(UserId memberId);

    /** Sets or removes the address; one that is not an email address is refused. */
    Optional<String> changeEmail(ChangeEmail command);
}
