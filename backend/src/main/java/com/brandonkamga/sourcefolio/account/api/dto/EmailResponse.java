package com.brandonkamga.sourcefolio.account.api.dto;

import java.util.Optional;

/** email is null when the member gave none. */
public record EmailResponse(String email) {

    public static EmailResponse of(Optional<String> email) {
        return new EmailResponse(email.orElse(null));
    }
}
