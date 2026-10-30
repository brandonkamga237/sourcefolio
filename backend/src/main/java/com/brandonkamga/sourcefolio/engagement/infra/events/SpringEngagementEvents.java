package com.brandonkamga.sourcefolio.engagement.infra.events;

import com.brandonkamga.sourcefolio.engagement.application.EngagementEvents;
import com.brandonkamga.sourcefolio.engagement.application.ShowcaseLikesChanged;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
class SpringEngagementEvents implements EngagementEvents {

    private final ApplicationEventPublisher publisher;

    SpringEngagementEvents(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void likesChanged(ShowcaseLikesChanged event) {
        publisher.publishEvent(event);
    }
}
