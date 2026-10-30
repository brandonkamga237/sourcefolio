package com.brandonkamga.sourcefolio.discovery.infra.events;

import com.brandonkamga.sourcefolio.account.application.MemberSignedIn;
import com.brandonkamga.sourcefolio.discovery.application.CatalogProjection;
import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogEntry;
import com.brandonkamga.sourcefolio.showcase.application.ShowcasePublished;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Feeds the catalogue from the events of showcase and account. After commit, so a rolled-back
 * publication never shows. A failure here is logged, not thrown: the publication itself
 * succeeded and the member must not see an error for the catalogue's copy.
 */
@Component
class CatalogListeners {

    private static final Logger log = LoggerFactory.getLogger(CatalogListeners.class);

    private final CatalogProjection catalog;

    CatalogListeners(CatalogProjection catalog) {
        this.catalog = catalog;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void on(ShowcasePublished event) {
        try {
            catalog.add(new CatalogEntry(event.showcaseId(), event.slug(), event.siteUrl(), event.repository(),
                    event.repositoryUrl(), event.ownerId(), event.ownerLogin(), event.publishedAt()));
        } catch (RuntimeException failure) {
            log.error("Showcase {} published but not added to the catalogue", event.showcaseId(), failure);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void on(MemberSignedIn event) {
        try {
            catalog.renameOwner(event.memberId(), event.login());
        } catch (RuntimeException failure) {
            log.error("Catalogue cards of member {} not renamed to {}", event.memberId(), event.login(), failure);
        }
    }
}
