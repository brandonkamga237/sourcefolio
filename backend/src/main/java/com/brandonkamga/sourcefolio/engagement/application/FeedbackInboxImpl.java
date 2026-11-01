package com.brandonkamga.sourcefolio.engagement.application;

import com.brandonkamga.sourcefolio.engagement.domain.port.FeedbackReadStore;
import com.brandonkamga.sourcefolio.engagement.domain.port.FeedbackStore;
import com.brandonkamga.sourcefolio.showcase.application.ShowcasePublication;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FeedbackInboxImpl implements FeedbackInbox {

    private final ShowcasePublication showcases;
    private final FeedbackStore feedback;
    private final FeedbackReadStore reads;
    private final Clock clock;

    public FeedbackInboxImpl(ShowcasePublication showcases, FeedbackStore feedback, FeedbackReadStore reads, Clock clock) {
        this.showcases = showcases;
        this.feedback = feedback;
        this.reads = reads;
        this.clock = clock;
    }

    @Override
    public ReceivedFeedback findReceived(UUID memberId, String cursor, int size) {
        Instant seenAt = reads.seenAt(memberId).orElse(null);
        return myShowcase(memberId)
                .map(showcaseId -> new ReceivedFeedback(FeedbackPages.read(feedback, showcaseId, cursor, size), seenAt,
                        feedback.countSince(showcaseId, seenAt)))
                .orElseGet(() -> new ReceivedFeedback(new FeedbackPage(List.of(), null, 0), seenAt, 0));
    }

    @Override
    public int countUnread(UUID memberId) {
        return myShowcase(memberId).map(showcaseId -> feedback.countSince(showcaseId, reads.seenAt(memberId).orElse(null))).orElse(0);
    }

    @Override
    public void markSeen(UUID memberId) {
        reads.markSeen(memberId, clock.instant());
    }

    private Optional<UUID> myShowcase(UUID memberId) {
        return showcases.findMyShowcase(memberId).map(showcase -> showcase.id().value());
    }
}
