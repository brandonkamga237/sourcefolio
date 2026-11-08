package com.brandonkamga.sourcefolio.showcase.infra.events;

import com.brandonkamga.sourcefolio.showcase.application.ShowcaseRemoved;
import com.brandonkamga.sourcefolio.storage.application.MediaStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Deletes a departing member's preview files once the account deletion is committed. */
@Component
class ShowcaseRemovedListener {

    private static final Logger log = LoggerFactory.getLogger(ShowcaseRemovedListener.class);

    private final MediaStore media;

    ShowcaseRemovedListener(MediaStore media) {
        this.media = media;
    }

    // One failure must not stop the other files: the account is already gone.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void on(ShowcaseRemoved event) {
        for (String key : event.mediaKeys()) {
            try {
                media.delete(key);
            } catch (RuntimeException failure) {
                log.warn("Preview file {} of deleted member {} not deleted", key, event.memberId(), failure);
            }
        }
    }
}
