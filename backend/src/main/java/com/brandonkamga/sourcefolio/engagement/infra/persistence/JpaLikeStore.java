package com.brandonkamga.sourcefolio.engagement.infra.persistence;

import com.brandonkamga.sourcefolio.engagement.domain.port.LikeStore;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
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

    @Override
    public List<UUID> removeAllBy(UUID memberId) {
        List<UUID> liked = reactions.lockLikedBy(memberId);
        reactions.deleteLikesBy(memberId);
        return liked;
    }

    @Override
    public Map<UUID, Integer> counts(Collection<UUID> showcaseIds) {
        if (showcaseIds.isEmpty()) {
            return Map.of();
        }
        return reactions.countLikes(showcaseIds).stream()
                .collect(Collectors.toMap(ReactionJpaRepository.LikeCount::getShowcaseId, row -> (int) row.getLikes()));
    }
}
