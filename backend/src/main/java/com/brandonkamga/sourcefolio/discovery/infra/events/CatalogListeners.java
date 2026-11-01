package com.brandonkamga.sourcefolio.discovery.infra.events;

import com.brandonkamga.sourcefolio.account.application.MemberSignedIn;
import com.brandonkamga.sourcefolio.discovery.application.CatalogProjection;
import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogEntry;
import com.brandonkamga.sourcefolio.engagement.application.ShowcaseLikesChanged;
import com.brandonkamga.sourcefolio.profile.application.ProfileChanged;
import com.brandonkamga.sourcefolio.showcase.application.ShowcasePreviewChanged;
import com.brandonkamga.sourcefolio.showcase.application.ShowcasePublished;
import com.brandonkamga.sourcefolio.showcase.application.ShowcaseRepositoryChanged;
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
    void on(ShowcasePreviewChanged event) {
        try {
            catalog.updatePreview(event.showcaseId(), event.imageUrl(), event.videoUrl());
        } catch (RuntimeException failure) {
            log.error("Preview of showcase {} not copied to the catalogue", event.showcaseId(), failure);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void on(ShowcaseLikesChanged event) {
        try {
            catalog.updateLikes(event.showcaseId(), event.count());
        } catch (RuntimeException failure) {
            log.error("Like count of showcase {} not copied to the catalogue", event.showcaseId(), failure);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void on(ShowcaseRepositoryChanged event) {
        try {
            catalog.updateRepository(event.showcaseId(), event.stars(), event.description(), event.languages(), event.pushedAt());
        } catch (RuntimeException failure) {
            log.error("Repository facts of showcase {} not copied to the catalogue", event.showcaseId(), failure);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void on(ProfileChanged event) {
        try {
            catalog.updateAuthorProfile(event.memberId(), event.countryCode(), event.specialties(), event.headline(), event.role());
        } catch (RuntimeException failure) {
            log.error("Profile of member {} not copied to the catalogue", event.memberId(), failure);
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
