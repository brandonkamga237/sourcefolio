package com.brandonkamga.sourcefolio.contribution.infra.events;

import com.brandonkamga.sourcefolio.contribution.application.ContributionAnalyzed;
import com.brandonkamga.sourcefolio.contribution.application.ContributionEvents;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
class SpringContributionEvents implements ContributionEvents {

    private final ApplicationEventPublisher publisher;

    SpringContributionEvents(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void analyzed(ContributionAnalyzed event) {
        publisher.publishEvent(event);
    }
}
