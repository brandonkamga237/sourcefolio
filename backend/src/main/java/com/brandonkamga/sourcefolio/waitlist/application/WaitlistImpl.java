package com.brandonkamga.sourcefolio.waitlist.application;

import com.brandonkamga.sourcefolio.shared.domain.EmailAddress;
import com.brandonkamga.sourcefolio.shared.exception.BadRequestException;
import com.brandonkamga.sourcefolio.waitlist.domain.port.WaitlistStore;
import java.time.Clock;

public class WaitlistImpl implements Waitlist {

    private final WaitlistStore store;
    private final Clock clock;

    public WaitlistImpl(WaitlistStore store, Clock clock) {
        this.store = store;
        this.clock = clock;
    }

    @Override
    public void join(JoinWaitlist command) {
        if (!command.consent()) {
            throw new BadRequestException("waitlist.consent-required");
        }
        EmailAddress email = EmailAddress.parse(command.email()).orElseThrow(() -> new BadRequestException("waitlist.email-invalid"));
        store.add(email, clock.instant());
    }
}
