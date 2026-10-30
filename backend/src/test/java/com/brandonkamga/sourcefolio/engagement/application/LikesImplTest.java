package com.brandonkamga.sourcefolio.engagement.application;

import com.brandonkamga.sourcefolio.engagement.domain.port.LikeStore;
import com.brandonkamga.sourcefolio.shared.exception.AppException;
import com.brandonkamga.sourcefolio.shared.exception.ErrorCode;
import com.brandonkamga.sourcefolio.showcase.application.PublicShowcase;
import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateReason;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseCandidate;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseSlug;
import com.brandonkamga.sourcefolio.showcase.domain.model.SiteUrl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LikesImplTest {

    private static final Instant NOW = Instant.parse("2026-10-30T09:00:00Z");
    private static final UUID OWNER = UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11");
    private static final UUID FAN = UUID.fromString("5d1e7a40-2b9c-4c55-8f0e-6a3b2c1d0e99");
    private static final Showcase SHOWCASE = Showcase.publishVerified(OWNER, new ShowcaseSlug("nekwalla"), new ShowcaseCandidate(
            new RepositoryCoordinates("nekwalla", "nekwalla.github.io"), new SiteUrl("https://nekwalla.github.io/"),
            CandidateReason.USER_PAGES), NOW).stored();

    private final Set<String> rows = new HashSet<>();
    private final LikeStore store = new LikeStore() {
        @Override
        public boolean add(UUID memberId, UUID showcaseId, Instant at) {
            return rows.add(memberId + ":" + showcaseId);
        }

        @Override
        public boolean remove(UUID memberId, UUID showcaseId) {
            return rows.remove(memberId + ":" + showcaseId);
        }

        @Override
        public int count(UUID showcaseId) {
            return (int) rows.stream().filter(row -> row.endsWith(":" + showcaseId)).count();
        }

        @Override
        public boolean exists(UUID memberId, UUID showcaseId) {
            return rows.contains(memberId + ":" + showcaseId);
        }
    };
    private final List<ShowcaseLikesChanged> announced = new ArrayList<>();
    private final Likes likes = new LikesImpl(
            slug -> slug.equals("nekwalla") ? Optional.of(new PublicShowcase(SHOWCASE, "nekwalla")) : Optional.empty(),
            store, announced::add, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    @DisplayName("a like counts once however many times it is sent, and the new count is announced once")
    void likeIsIdempotent() {
        assertThat(likes.like(FAN, "nekwalla")).isEqualTo(new LikeState(1, true));
        assertThat(likes.like(FAN, "nekwalla")).isEqualTo(new LikeState(1, true));

        assertThat(announced).containsExactly(new ShowcaseLikesChanged(SHOWCASE.id().value(), 1));
    }

    @Test
    @DisplayName("taking a like back lowers the count; taking it back twice changes nothing more")
    void unlike() {
        likes.like(FAN, "nekwalla");

        assertThat(likes.unlike(FAN, "nekwalla")).isEqualTo(new LikeState(0, false));
        assertThat(likes.unlike(FAN, "nekwalla")).isEqualTo(new LikeState(0, false));
        assertThat(announced).extracting(ShowcaseLikesChanged::count).containsExactly(1, 0);
    }

    @Test
    @DisplayName("a visitor sees the count without a liked state; a member sees their own")
    void state() {
        likes.like(FAN, "nekwalla");

        assertThat(likes.findState(null, "nekwalla")).isEqualTo(new LikeState(1, false));
        assertThat(likes.findState(FAN, "nekwalla")).isEqualTo(new LikeState(1, true));
    }

    @Test
    @DisplayName("an author cannot like their own portfolio")
    void notOwnShowcase() {
        assertThatThrownBy(() -> likes.like(OWNER, "nekwalla"))
                .isInstanceOf(AppException.class)
                .extracting(thrown -> ((AppException) thrown).code()).isEqualTo(ErrorCode.FORBIDDEN);
        assertThat(rows).isEmpty();
    }

    @Test
    @DisplayName("an unknown or removed showcase cannot be liked")
    void unknownShowcase() {
        assertThatThrownBy(() -> likes.like(FAN, "nobody"))
                .isInstanceOf(AppException.class)
                .extracting(thrown -> ((AppException) thrown).code()).isEqualTo(ErrorCode.NOT_FOUND);
    }
}
