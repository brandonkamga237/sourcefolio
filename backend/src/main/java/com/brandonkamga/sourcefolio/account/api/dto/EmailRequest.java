package com.brandonkamga.sourcefolio.account.api.dto;

import com.brandonkamga.sourcefolio.account.application.ChangeEmail;
import com.brandonkamga.sourcefolio.account.domain.model.UserId;

/** email null or blank removes the address. */
public record EmailRequest(String email) {

    public ChangeEmail toCommand(UserId memberId) {
        return new ChangeEmail(memberId, email);
    }
}
