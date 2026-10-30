package com.brandonkamga.sourcefolio.profile.infra.config;

import com.brandonkamga.sourcefolio.profile.application.ProfileCompletion;
import com.brandonkamga.sourcefolio.profile.application.ProfileCompletionImpl;
import com.brandonkamga.sourcefolio.profile.domain.port.ProfileStore;
import com.brandonkamga.sourcefolio.shared.config.TransactionalServices;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ProfileServices {

    @Bean
    ProfileCompletion profileCompletion(ProfileStore profiles, Clock clock, TransactionalServices transactional) {
        return transactional.wrap(ProfileCompletion.class, new ProfileCompletionImpl(profiles, clock));
    }
}
