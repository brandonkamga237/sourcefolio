package com.brandonkamga.sourcefolio.showcase.application;

import com.brandonkamga.sourcefolio.shared.exception.AppException;
import com.brandonkamga.sourcefolio.shared.exception.ErrorCode;
import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateReason;
import com.brandonkamga.sourcefolio.showcase.domain.model.PreviewMedia;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseCandidate;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseSlug;
import com.brandonkamga.sourcefolio.showcase.domain.model.SiteUrl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShowcaseRemovalImplTest {

    private static final Instant NOW = Instant.parse("2026-10-30T09:00:00Z");
    private static final UUID MEMBER = UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11");
    private static final UUID OTHER = UUID.fromString("5d1e7a40-2b9c-4c55-8f0e-6a3b2c1d0e99");

    private final ShowcasePublicationImplTest.InMemoryShowcaseStore showcases = new ShowcasePublicationImplTest.InMemoryShowcaseStore();
    private final List<ShowcaseRemoved> removed = new ArrayList<>();
    private final ShowcaseRemoval removal = new ShowcaseRemovalImpl(showcases, new ShowcaseEvents() {
        @Override
        public void showcasePublished(ShowcasePublished event) {
            throw new AssertionError("removing publishes nothing");
        }

        @Override
        public void previewChanged(ShowcasePreviewChanged event) {
            throw new AssertionError("removing changes no preview");
        }

        @Override
        public void showcaseRemoved(ShowcaseRemoved event) {
            removed.add(event);
        }

        @Override
        public void starsChanged(ShowcaseStarsChanged event) {
            throw new AssertionError("removing reads no stars");
        }
    });

    @Test
    @DisplayName("withdrawing deletes the member's portfolio and hands over its preview files for deletion")
    void withdraw() {
        Showcase mine = showcases.save(published(MEMBER, "nekwalla")
                .withPreviewImage(new PreviewMedia("previews/a/1.webp", "https://media.test/previews/a/1.webp"))
                .withPreviewVideo(new PreviewMedia("previews/a/2.mp4", "https://media.test/previews/a/2.mp4")));
        Showcase theirs = showcases.save(published(OTHER, "ayo-dev"));

        removal.withdraw(MEMBER);

        assertThat(showcases.rows.keySet()).containsExactly(theirs.id().value());
        assertThat(removed).containsExactly(new ShowcaseRemoved(mine.id().value(), List.of("previews/a/1.webp", "previews/a/2.mp4")));
    }

    @Test
    @DisplayName("withdrawing without a portfolio is a 404")
    void withdrawNothing() {
        assertThatThrownBy(() -> removal.withdraw(MEMBER))
                .isInstanceOf(AppException.class)
                .extracting(thrown -> ((AppException) thrown).code()).isEqualTo(ErrorCode.NOT_FOUND);
        assertThat(removed).isEmpty();
    }

    @Test
    @DisplayName("an account deletion removes the portfolio if there is one, and is quiet otherwise")
    void removeAllOf() {
        removal.removeAllOf(MEMBER);
        assertThat(removed).isEmpty();

        Showcase mine = showcases.save(published(MEMBER, "nekwalla"));
        removal.removeAllOf(MEMBER);

        assertThat(showcases.rows).isEmpty();
        assertThat(removed).containsExactly(new ShowcaseRemoved(mine.id().value(), List.of()));
    }

    private static Showcase published(UUID owner, String login) {
        return Showcase.publishVerified(owner, new ShowcaseSlug(login), new ShowcaseCandidate(
                new RepositoryCoordinates(login, login + ".github.io"), new SiteUrl("https://" + login + ".github.io/"),
                CandidateReason.USER_PAGES), NOW);
    }
}
