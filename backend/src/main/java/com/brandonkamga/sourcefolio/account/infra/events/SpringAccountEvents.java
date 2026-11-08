package com.brandonkamga.sourcefolio.account.infra.events;

import com.brandonkamga.sourcefolio.account.application.MemberSignedIn;
import com.brandonkamga.sourcefolio.account.application.AccountEvents;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
class SpringAccountEvents implements AccountEvents {

    private final ApplicationEventPublisher publisher;

    SpringAccountEvents(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void memberSignedIn(MemberSignedIn event) {
        publisher.publishEvent(event);
    }
}
