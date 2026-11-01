package com.brandonkamga.sourcefolio.engagement.infra.persistence;

import com.brandonkamga.sourcefolio.engagement.domain.port.FeedbackReadStore;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class JpaFeedbackReadStore implements FeedbackReadStore {

    private final FeedbackReadJpaRepository reads;

    JpaFeedbackReadStore(FeedbackReadJpaRepository reads) {
        this.reads = reads;
    }

    @Override
    public Optional<Instant> seenAt(UUID memberId) {
        return reads.findById(memberId).map(FeedbackReadEntity::getSeenAt);
    }

    // The id is assigned: save merges, so a second read updates the row instead of failing.
    @Override
    public void markSeen(UUID memberId, Instant at) {
        reads.save(new FeedbackReadEntity(memberId, at));
    }
}
