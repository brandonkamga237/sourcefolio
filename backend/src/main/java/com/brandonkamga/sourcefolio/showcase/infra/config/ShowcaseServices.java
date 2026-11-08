package com.brandonkamga.sourcefolio.showcase.infra.config;

import com.brandonkamga.sourcefolio.shared.config.TransactionalServices;
import com.brandonkamga.sourcefolio.showcase.application.ShowcaseEvents;
import com.brandonkamga.sourcefolio.showcase.application.ShowcaseRemoval;
import com.brandonkamga.sourcefolio.showcase.application.ShowcaseRemovalImpl;
import com.brandonkamga.sourcefolio.showcase.domain.port.ShowcaseStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ShowcaseServices {

    @Bean
    ShowcaseRemoval showcaseRemoval(ShowcaseStore showcases, ShowcaseEvents events, TransactionalServices transactional) {
        return transactional.wrap(ShowcaseRemoval.class, new ShowcaseRemovalImpl(showcases, events));
    }
}
