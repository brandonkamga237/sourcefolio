package com.brandonkamga.sourcefolio.waitlist.application;

public interface Waitlist {

    /** Signing up twice changes nothing and is not an error: the answer never reveals who is listed. */
    void join(JoinWaitlist command);

    /** Removing an address that is not listed is not an error either, for the same reason. */
    void leave(String email);
}
