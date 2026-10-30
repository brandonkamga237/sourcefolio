package com.brandonkamga.sourcefolio.discovery.infra.persistence;

import com.brandonkamga.sourcefolio.account.domain.model.User;
import com.brandonkamga.sourcefolio.account.domain.port.UserStore;
import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogEntry;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankedEntry;
import com.brandonkamga.sourcefolio.discovery.domain.model.RecentCursor;
import com.brandonkamga.sourcefolio.discovery.domain.model.Shuffled;
import com.brandonkamga.sourcefolio.discovery.domain.port.CatalogStore;
import com.brandonkamga.sourcefolio.shared.domain.Language;
import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateReason;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseCandidate;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseSlug;
import com.brandonkamga.sourcefolio.showcase.domain.model.SiteUrl;
import com.brandonkamga.sourcefolio.showcase.domain.port.ShowcaseStore;
import com.brandonkamga.sourcefolio.support.PostgresIT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

// Not @Transactional: the store's writes open their own transaction (REQUIRES_NEW) and commit,
// so each test cleans up after itself.
class JpaCatalogStoreIT extends PostgresIT {

    private static final Instant NOW = Instant.parse("2026-10-30T09:00:00Z");
    private static final LocalDate DAY = LocalDate.parse("2026-10-30");

    @Autowired
    private CatalogStore catalog;

    @Autowired
    private ShowcaseStore showcases;

    @Autowired
    private UserStore users;

    @Autowired
    private JdbcTemplate jdbc;

    private final List<UUID> created = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        created.forEach(owner -> jdbc.update("DELETE FROM users WHERE id = ?", owner));
    }

    @Test
    @DisplayName("an entry is stored once, however many times the event is delivered, and reads back as written")
    void upsertIsIdempotent() {
        CatalogEntry entry = entry("solo", NOW);

        catalog.upsert(entry);
        catalog.upsert(entry);

        assertThat(catalog.recent(null, 10)).containsExactly(entry);
        assertThat(catalog.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("a rename reaches every card of the member")
    void rename() {
        CatalogEntry entry = entry("renamed", NOW);
        catalog.upsert(entry);

        catalog.renameOwner(entry.ownerId(), "new-login");

        assertThat(catalog.recent(null, 10)).singleElement().extracting(CatalogEntry::ownerLogin).isEqualTo("new-login");
    }

    @Test
    @DisplayName("the shuffle splits young and older entries, orders them by key, and continues after a key")
    void shuffled() {
        Instant youngSince = NOW.minus(Duration.ofDays(14));
        for (int index = 0; index < 6; index++) catalog.upsert(entry("old" + index, NOW.minus(Duration.ofDays(30))));
        for (int index = 0; index < 3; index++) catalog.upsert(entry("new" + index, NOW.minus(Duration.ofDays(1))));

        List<Shuffled> old = catalog.shuffled(DAY, false, youngSince, "", 10);
        List<Shuffled> young = catalog.shuffled(DAY, true, youngSince, "", 10);
        List<Shuffled> rest = catalog.shuffled(DAY, false, youngSince, old.get(2).key(), 10);

        assertThat(old).hasSize(6).allSatisfy(item -> assertThat(item.entry().slug()).startsWith("old"));
        assertThat(young).hasSize(3).allSatisfy(item -> assertThat(item.entry().slug()).startsWith("new"));
        assertThat(old).extracting(Shuffled::key).isSorted().allMatch(key -> key.matches("[0-9a-f]{32}"));
        assertThat(rest).containsExactlyElementsOf(old.subList(3, 6));
        assertThat(catalog.shuffled(DAY.plusDays(1), false, youngSince, "", 10)).extracting(item -> item.entry().slug())
                .containsExactlyInAnyOrderElementsOf(old.stream().map(item -> item.entry().slug()).toList());
    }

    @Test
    @DisplayName("Recent pages newest first and continues exactly after its cursor, ties included")
    void recentKeyset() {
        CatalogEntry a = entry("a", NOW.minus(Duration.ofDays(2)));
        CatalogEntry b = entry("b", NOW);
        CatalogEntry c = entry("c", NOW);
        List.of(a, b, c).forEach(catalog::upsert);

        List<CatalogEntry> first = catalog.recent(null, 2);
        CatalogEntry last = first.getLast();
        List<CatalogEntry> second = catalog.recent(new RecentCursor(last.publishedAt(), last.showcaseId()), 2);

        assertThat(first).hasSize(2).allMatch(entry -> entry.publishedAt().equals(NOW));
        assertThat(second).containsExactly(a);
    }

    @Test
    @DisplayName("the preview chosen by the author is kept on the card, and re-publishing does not erase it")
    void preview() {
        CatalogEntry entry = entry("with-preview", NOW);
        catalog.upsert(entry);

        catalog.setPreview(entry.showcaseId(), "https://media.test/i.webp", "https://media.test/v.mp4");
        catalog.upsert(entry);

        assertThat(catalog.recent(null, 10)).singleElement().satisfies(card -> {
            assertThat(card.previewImageUrl()).isEqualTo("https://media.test/i.webp");
            assertThat(card.previewVideoUrl()).isEqualTo("https://media.test/v.mp4");
        });
    }

    @Test
    @DisplayName("the Ranking reads the like counts, most liked first, and leaves out unliked showcases")
    void ranking() {
        CatalogEntry popular = entry("popular", NOW.minus(Duration.ofDays(3)));
        CatalogEntry liked = entry("liked", NOW);
        CatalogEntry quiet = entry("quiet", NOW);
        List.of(popular, liked, quiet).forEach(catalog::upsert);

        catalog.setLikes(popular.showcaseId(), 5);
        catalog.setLikes(liked.showcaseId(), 2);

        assertThat(catalog.ranking(10)).extracting(RankedEntry::likes, ranked -> ranked.entry().slug())
                .containsExactly(org.assertj.core.groups.Tuple.tuple(5, "popular"), org.assertj.core.groups.Tuple.tuple(2, "liked"));
    }

    // A real showcase and member behind each entry, as the foreign keys require.
    private CatalogEntry entry(String slug, Instant publishedAt) {
        UUID owner = users.save(User.register(Language.FR, NOW)).id().value();
        created.add(owner);
        Showcase showcase = showcases.save(Showcase.publishVerified(owner, new ShowcaseSlug(slug + "-" + owner.toString().substring(0, 8)),
                new ShowcaseCandidate(new RepositoryCoordinates("o" + owner.toString().substring(0, 8), slug),
                        new SiteUrl("https://" + slug + ".example.org/"), CandidateReason.NAMED_REPOSITORY), publishedAt));
        return new CatalogEntry(showcase.id().value(), slug, showcase.siteUrl().value(), showcase.sourceRepo().coordinates().toString(),
                showcase.sourceRepo().url(), owner, "login-" + slug, publishedAt);
    }
}
