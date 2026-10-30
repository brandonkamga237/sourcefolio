package com.brandonkamga.sourcefolio.showcase.application;

import com.brandonkamga.sourcefolio.showcase.application.ShowcasePublicationImplTest.InMemoryShowcaseStore;
import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateReason;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseCandidate;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseSlug;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseStatus;
import com.brandonkamga.sourcefolio.showcase.domain.model.SiteUrl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class PublicShowcasesImplTest {

    private static final Instant NOW = Instant.parse("2026-10-30T09:00:00Z");
    private static final UUID MEMBER = UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11");

    private final InMemoryShowcaseStore store = new InMemoryShowcaseStore();
    private final PublicShowcases showcases = new PublicShowcasesImpl(store, Map.of(MEMBER, "Nekwalla")::get);

    @Test
    @DisplayName("a published showcase is found by its address, with its author's current login")
    void found() {
        Showcase stored = store.save(published());

        assertThat(showcases.findBySlug("nekwalla")).contains(new PublicShowcase(stored, "Nekwalla"));
    }

    @ParameterizedTest
    @EnumSource(value = ShowcaseStatus.class, names = {"UNAVAILABLE", "NEEDS_FIX"})
    @DisplayName("an unavailable or to-fix showcase stays reachable by its link")
    void hiddenButReachable(ShowcaseStatus status) {
        store.save(withStatus(published(), status));

        assertThat(showcases.findBySlug("nekwalla")).isPresent();
    }

    @Test
    @DisplayName("a removed showcase is gone for visitors")
    void removedIsGone() {
        store.save(withStatus(published(), ShowcaseStatus.REMOVED));

        assertThat(showcases.findBySlug("nekwalla")).isEmpty();
    }

    @Test
    @DisplayName("an unknown or malformed address finds nothing")
    void unknown() {
        assertThat(showcases.findBySlug("nobody")).isEmpty();
        assertThat(showcases.findBySlug("../etc")).isEmpty();
    }

    private static Showcase published() {
        return Showcase.publishVerified(MEMBER, new ShowcaseSlug("nekwalla"), new ShowcaseCandidate(
                new RepositoryCoordinates("nekwalla", "nekwalla.github.io"), new SiteUrl("https://nekwalla.github.io/"),
                CandidateReason.USER_PAGES), NOW);
    }

    private static Showcase withStatus(Showcase showcase, ShowcaseStatus status) {
        return new Showcase(showcase.id(), showcase.slug(), showcase.ownerId(), showcase.kind(), showcase.siteUrl(),
                showcase.sourceRepo(), status, showcase.createdAt(), showcase.publishedAt(), List.of());
    }
}
