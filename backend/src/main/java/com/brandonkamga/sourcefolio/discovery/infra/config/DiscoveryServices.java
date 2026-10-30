package com.brandonkamga.sourcefolio.discovery.infra.config;

import com.brandonkamga.sourcefolio.discovery.application.Catalog;
import com.brandonkamga.sourcefolio.discovery.application.CatalogImpl;
import com.brandonkamga.sourcefolio.discovery.application.CatalogProjection;
import com.brandonkamga.sourcefolio.discovery.application.CatalogProjectionImpl;
import com.brandonkamga.sourcefolio.discovery.domain.port.CatalogStore;
import com.brandonkamga.sourcefolio.shared.config.TransactionalServices;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class DiscoveryServices {

    @Bean
    Catalog catalog(CatalogStore store, Clock clock, TransactionalServices transactional) {
        return transactional.wrap(Catalog.class, new CatalogImpl(store, clock));
    }

    // Not wrapped: it runs after another module's commit, and the store opens a new transaction
    // for each write (see JpaCatalogStore).
    @Bean
    CatalogProjection catalogProjection(CatalogStore store) {
        return new CatalogProjectionImpl(store);
    }
}
