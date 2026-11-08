package com.brandonkamga.sourcefolio.waitlist.domain.port;

import com.brandonkamga.sourcefolio.shared.domain.EmailAddress;
import java.time.Instant;

public interface WaitlistStore {

    /** An address already listed, whatever its capitals, keeps its first consent date. */
    void add(EmailAddress email, Instant consentedAt);
}
