package com.brandonkamga.sourcefolio.showcase.infra.events;

import com.brandonkamga.sourcefolio.showcase.application.ShowcaseEvents;
import com.brandonkamga.sourcefolio.showcase.application.ShowcasePreviewChanged;
import com.brandonkamga.sourcefolio.showcase.application.ShowcasePublished;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
class SpringShowcaseEvents implements ShowcaseEvents {

    private final ApplicationEventPublisher publisher;

    SpringShowcaseEvents(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void showcasePublished(ShowcasePublished event) {
        publisher.publishEvent(event);
    }

    @Override
    public void previewChanged(ShowcasePreviewChanged event) {
        publisher.publishEvent(event);
    }
}
