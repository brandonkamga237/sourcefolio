package com.brandonkamga.sourcefolio.profile.infra.events;

import com.brandonkamga.sourcefolio.profile.application.ProfileChanged;
import com.brandonkamga.sourcefolio.profile.application.ProfileEvents;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
class SpringProfileEvents implements ProfileEvents {

    private final ApplicationEventPublisher publisher;

    SpringProfileEvents(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void profileChanged(ProfileChanged event) {
        publisher.publishEvent(event);
    }
}
