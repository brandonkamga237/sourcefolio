package com.brandonkamga.sourcefolio.waitlist.infra.config;

import com.brandonkamga.sourcefolio.shared.config.TransactionalServices;
import com.brandonkamga.sourcefolio.waitlist.application.Waitlist;
import com.brandonkamga.sourcefolio.waitlist.application.WaitlistImpl;
import com.brandonkamga.sourcefolio.waitlist.domain.port.WaitlistStore;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class WaitlistServices {

    @Bean
    Waitlist waitlist(WaitlistStore store, Clock clock, TransactionalServices transactional) {
        return transactional.wrap(Waitlist.class, new WaitlistImpl(store, clock));
    }
}
