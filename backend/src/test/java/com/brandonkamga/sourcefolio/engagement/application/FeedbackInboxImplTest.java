package com.brandonkamga.sourcefolio.engagement.application;

import com.brandonkamga.sourcefolio.engagement.domain.model.Feedback;
import com.brandonkamga.sourcefolio.engagement.domain.model.FeedbackBody;
import com.brandonkamga.sourcefolio.engagement.domain.port.FeedbackReadStore;
import com.brandonkamga.sourcefolio.shared.domain.Language;
import com.brandonkamga.sourcefolio.showcase.application.PublishShowcase;
import com.brandonkamga.sourcefolio.showcase.application.ShowcasePublication;
import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateReason;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseCandidate;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseSlug;
import com.brandonkamga.sourcefolio.showcase.domain.model.SiteUrl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class FeedbackInboxImplTest {

    private static final Instant NOW = Instant.parse("2026-11-01T09:00:00Z");
    private static final UUID AUTHOR = UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11");
    private static final UUID READER = UUID.fromString("5d1e7a40-2b9c-4c55-8f0e-6a3b2c1d0e99");
    private static final UUID NEWCOMER = UUID.fromString("9a0e3f11-6b7c-4d2e-8f90-1a2b3c4d5e6f");
    private static final Showcase SHOWCASE = Showcase.publishVerified(AUTHOR, new ShowcaseSlug("nekwalla"), new ShowcaseCandidate(
            new RepositoryCoordinates("nekwalla", "nekwalla.github.io"), new SiteUrl("https://nekwalla.github.io/"),
            CandidateReason.USER_PAGES), NOW).stored();

    private final InMemoryFeedbackStore feedback = new InMemoryFeedbackStore();
    private final Map<UUID, Instant> seen = new HashMap<>();
    private final FeedbackReadStore reads = new FeedbackReadStore() {
        @Override
        public Optional<Instant> seenAt(UUID memberId) {
            return Optional.ofNullable(seen.get(memberId));
        }

        @Override
        public void markSeen(UUID memberId, Instant at) {
            seen.put(memberId, at);
        }
    };
    private final ShowcasePublication showcases = new ShowcasePublication() {
        @Override
        public Showcase publish(PublishShowcase command) {
            throw new AssertionError("the inbox publishes nothing");
        }

        @Override
        public Optional<Showcase> findMyShowcase(UUID memberId) {
            return memberId.equals(AUTHOR) ? Optional.of(SHOWCASE) : Optional.empty();
        }
    };
    private Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private FeedbackInbox inbox() {
        return new FeedbackInboxImpl(showcases, feedback, reads, clock);
    }

    @Test
    @DisplayName("an author reads the feedback on their portfolio, newest first; the first time, all of it is new")
    void firstRead() {
        received("Le menu déborde sur mon téléphone.", NOW.minus(Duration.ofHours(2)));
        received("Très clair, j’ai compris en 10 secondes.", NOW.minus(Duration.ofHours(1)));

        ReceivedFeedback inbox = inbox().findReceived(AUTHOR, null, 10);

        assertThat(inbox.page().items()).extracting(item -> item.body().value())
                .containsExactly("Très clair, j’ai compris en 10 secondes.", "Le menu déborde sur mon téléphone.");
        assertThat(inbox.seenAt()).isNull();
        assertThat(inbox.unread()).isEqualTo(2);
        assertThat(inbox().countUnread(AUTHOR)).isEqualTo(2);
    }

    @Test
    @DisplayName("once read, only the feedback written afterwards counts as new")
    void afterReading() {
        received("Le menu déborde sur mon téléphone.", NOW.minus(Duration.ofHours(2)));
        inbox().markSeen(AUTHOR);
        clock = Clock.fixed(NOW.plus(Duration.ofHours(1)), ZoneOffset.UTC);
        received("Le README mériterait une section setup.", NOW.plus(Duration.ofMinutes(30)));

        assertThat(inbox().countUnread(AUTHOR)).isEqualTo(1);
        assertThat(inbox().findReceived(AUTHOR, null, 10).seenAt()).isEqualTo(NOW);
        assertThat(inbox().findReceived(AUTHOR, null, 10).page().total()).isEqualTo(2);
    }

    @Test
    @DisplayName("a member without a portfolio has nothing to read and nothing new")
    void noPortfolio() {
        received("Le menu déborde sur mon téléphone.", NOW);

        assertThat(inbox().findReceived(NEWCOMER, null, 10).page().items()).isEmpty();
        assertThat(inbox().countUnread(NEWCOMER)).isZero();
    }

    private void received(String body, Instant at) {
        feedback.add(new Feedback(UUID.randomUUID(), SHOWCASE.id().value(), READER, "ada", null, FeedbackBody.parse(body), Language.FR, at));
    }
}
