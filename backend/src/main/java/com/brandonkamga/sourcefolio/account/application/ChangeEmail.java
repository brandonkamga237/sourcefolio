package com.brandonkamga.sourcefolio.account.application;

import com.brandonkamga.sourcefolio.account.domain.model.UserId;
import java.util.Objects;

/** A new address for alerts; blank or null removes it. */
public record ChangeEmail(UserId memberId, String email) {

    public ChangeEmail {
        Objects.requireNonNull(memberId, "memberId");
    }
}
