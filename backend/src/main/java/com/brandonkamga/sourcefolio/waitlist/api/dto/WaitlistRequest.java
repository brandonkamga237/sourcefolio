package com.brandonkamga.sourcefolio.waitlist.api.dto;

import com.brandonkamga.sourcefolio.waitlist.application.JoinWaitlist;

public record WaitlistRequest(String email, boolean consent) {

    public JoinWaitlist toCommand() {
        return new JoinWaitlist(email, consent);
    }
}
