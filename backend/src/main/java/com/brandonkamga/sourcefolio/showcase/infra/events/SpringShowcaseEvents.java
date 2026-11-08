package com.brandonkamga.sourcefolio.showcase.infra.events;

import com.brandonkamga.sourcefolio.showcase.application.ShowcaseEvents;
import com.brandonkamga.sourcefolio.showcase.application.ShowcaseRemoved;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
class SpringShowcaseEvents implements ShowcaseEvents {

    private final ApplicationEventPublisher publisher;

    SpringShowcaseEvents(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void showcaseRemoved(ShowcaseRemoved event) {
        publisher.publishEvent(event);
    }
}
