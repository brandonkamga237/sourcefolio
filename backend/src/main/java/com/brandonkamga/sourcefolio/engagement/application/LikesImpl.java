package com.brandonkamga.sourcefolio.engagement.application;

import com.brandonkamga.sourcefolio.engagement.domain.port.LikeStore;
import com.brandonkamga.sourcefolio.shared.exception.ForbiddenException;
import com.brandonkamga.sourcefolio.shared.exception.NotFoundException;
import com.brandonkamga.sourcefolio.showcase.application.PublicShowcase;
import com.brandonkamga.sourcefolio.showcase.application.PublicShowcases;
import java.time.Clock;
import java.util.UUID;

public class LikesImpl implements Likes {

    private final PublicShowcases showcases;
    private final LikeStore likes;
    private final EngagementEvents events;
    private final Clock clock;

    public LikesImpl(PublicShowcases showcases, LikeStore likes, EngagementEvents events, Clock clock) {
        this.showcases = showcases;
        this.likes = likes;
        this.events = events;
        this.clock = clock;
    }

    @Override
    public LikeState findState(UUID memberId, String slug) {
        UUID showcaseId = showcaseIdOf(require(slug));
        return new LikeState(likes.count(showcaseId), memberId != null && likes.exists(memberId, showcaseId));
    }

    @Override
    public LikeState like(UUID memberId, String slug) {
        PublicShowcase found = require(slug);
        if (found.showcase().ownerId().equals(memberId)) {
            throw new ForbiddenException("engagement.own-showcase");
        }
        UUID showcaseId = showcaseIdOf(found);
        boolean added = likes.add(memberId, showcaseId, clock.instant());
        return stateAfter(showcaseId, added, true);
    }

    @Override
    public LikeState unlike(UUID memberId, String slug) {
        UUID showcaseId = showcaseIdOf(require(slug));
        boolean removed = likes.remove(memberId, showcaseId);
        return stateAfter(showcaseId, removed, false);
    }

    // The count is announced only when it moved, so a repeated tap sends nothing.
    private LikeState stateAfter(UUID showcaseId, boolean changed, boolean liked) {
        int count = likes.count(showcaseId);
        if (changed) {
            events.likesChanged(new ShowcaseLikesChanged(showcaseId, count));
        }
        return new LikeState(count, liked);
    }

    private PublicShowcase require(String slug) {
        return showcases.findBySlug(slug).orElseThrow(() -> new NotFoundException("showcase.not-found"));
    }

    private static UUID showcaseIdOf(PublicShowcase found) {
        return found.showcase().id().value();
    }
}
