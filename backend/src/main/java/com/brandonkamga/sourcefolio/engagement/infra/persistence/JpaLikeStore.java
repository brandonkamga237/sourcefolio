package com.brandonkamga.sourcefolio.engagement.infra.persistence;

import com.brandonkamga.sourcefolio.engagement.domain.port.LikeStore;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class JpaLikeStore implements LikeStore {

    private final ReactionJpaRepository reactions;

    JpaLikeStore(ReactionJpaRepository reactions) {
        this.reactions = reactions;
    }

    @Override
    public boolean add(UUID memberId, UUID showcaseId, Instant at) {
        return reactions.insertLike(memberId, showcaseId, at) == 1;
    }

    @Override
    public boolean remove(UUID memberId, UUID showcaseId) {
        return reactions.deleteLike(memberId, showcaseId) == 1;
    }

    @Override
    public int count(UUID showcaseId) {
        return reactions.countLikes(showcaseId);
    }

    @Override
    public boolean exists(UUID memberId, UUID showcaseId) {
        return reactions.likeExists(memberId, showcaseId);
    }
}
