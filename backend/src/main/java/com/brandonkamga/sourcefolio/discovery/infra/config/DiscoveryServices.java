package com.brandonkamga.sourcefolio.discovery.infra.config;

import com.brandonkamga.sourcefolio.discovery.application.RankingProjection;
import com.brandonkamga.sourcefolio.discovery.application.RankingProjectionImpl;
import com.brandonkamga.sourcefolio.discovery.application.Rankings;
import com.brandonkamga.sourcefolio.discovery.application.RankingsImpl;
import com.brandonkamga.sourcefolio.discovery.domain.port.RankingStore;
import com.brandonkamga.sourcefolio.shared.config.TransactionalServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class DiscoveryServices {

    @Bean
    RankingProjection rankingProjection(RankingStore store) {
        return new RankingProjectionImpl(store);
    }

    @Bean
    Rankings rankings(RankingStore store, TransactionalServices transactional) {
        return transactional.wrap(Rankings.class, new RankingsImpl(store));
    }
}
