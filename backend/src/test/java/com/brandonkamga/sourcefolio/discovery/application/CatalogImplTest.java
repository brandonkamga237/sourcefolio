package com.brandonkamga.sourcefolio.discovery.application;

import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogEntry;
import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class CatalogImplTest {

    private static final Instant NOW = Instant.parse("2026-10-30T09:00:00Z");

    private final InMemoryCatalogStore store = new InMemoryCatalogStore();
    private Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private Catalog catalog() {
        return new CatalogImpl(store, clock);
    }

    @Test
    @DisplayName("scrolling Discover to the end shows every published showcase exactly once")
    void discoverCoversEverythingOnce() {
        List<String> all = new ArrayList<>();
        for (int index = 0; index < 23; index++) all.add(add("old" + index, NOW.minus(Duration.ofDays(40 + index))).slug());
        for (int index = 0; index < 7; index++) all.add(add("new" + index, NOW.minus(Duration.ofDays(index))).slug());

        List<String> seen = new ArrayList<>();
        String cursor = null;
        do {
            CatalogPage page = catalog().listDiscover(cursor, 4);
            page.entries().forEach(entry -> seen.add(entry.slug()));
            cursor = page.nextCursor();
        } while (cursor != null);

        assertThat(seen).hasSize(30).containsExactlyInAnyOrderElementsOf(all);
    }

    @Test
    @DisplayName("in Discover, every fifth card is a showcase younger than 14 days while there are some")
    void youngEveryFifth() {
        for (int index = 0; index < 20; index++) add("old" + index, NOW.minus(Duration.ofDays(30)));
        for (int index = 0; index < 4; index++) add("new" + index, NOW.minus(Duration.ofDays(2)));

        List<CatalogEntry> feed = catalog().listDiscover(null, 20).entries();

        for (int position = 4; position < 20; position += 5) {
            assertThat(feed.get(position).slug()).as("position %d", position).startsWith("new");
        }
        assertThat(feed).filteredOn(entry -> entry.slug().startsWith("new")).hasSize(4);
    }

    @Test
    @DisplayName("the shuffle is the same all day and changes the next day")
    void shuffledOncePerDay() {
        for (int index = 0; index < 12; index++) add("old" + index, NOW.minus(Duration.ofDays(30)));

        List<String> morning = slugs(catalog().listDiscover(null, 12));
        clock = Clock.fixed(NOW.plus(Duration.ofHours(10)), ZoneOffset.UTC);
        List<String> evening = slugs(catalog().listDiscover(null, 12));
        clock = Clock.fixed(NOW.plus(Duration.ofDays(1)), ZoneOffset.UTC);
        List<String> tomorrow = slugs(catalog().listDiscover(null, 12));

        assertThat(evening).isEqualTo(morning);
        assertThat(tomorrow).containsExactlyInAnyOrderElementsOf(morning).isNotEqualTo(morning);
    }

    @Test
    @DisplayName("a cursor keeps its day: scrolling past midnight neither repeats nor skips a card")
    void cursorKeepsItsDay() {
        for (int index = 0; index < 10; index++) add("old" + index, NOW.minus(Duration.ofDays(30)));

        CatalogPage first = catalog().listDiscover(null, 5);
        clock = Clock.fixed(NOW.plus(Duration.ofDays(1)), ZoneOffset.UTC);
        CatalogPage second = catalog().listDiscover(first.nextCursor(), 5);

        assertThat(slugs(first)).doesNotContainAnyElementsOf(slugs(second));
        assertThat(second.nextCursor()).isNull();
    }

    @Test
    @DisplayName("Recent shows the newest first, page after page, and says when it is over")
    void recentNewestFirst() {
        add("a", NOW.minus(Duration.ofDays(3)));
        add("b", NOW.minus(Duration.ofDays(1)));
        add("c", NOW.minus(Duration.ofDays(2)));

        CatalogPage first = catalog().listRecent(null, 2);
        CatalogPage second = catalog().listRecent(first.nextCursor(), 2);

        assertThat(slugs(first)).containsExactly("b", "c");
        assertThat(slugs(second)).containsExactly("a");
        assertThat(second.nextCursor()).isNull();
    }

    @Test
    @DisplayName("a page size outside 1 to 50 is brought back within it")
    void pageSizeClamped() {
        for (int index = 0; index < 60; index++) add("old" + index, NOW.minus(Duration.ofDays(30)));

        assertThat(catalog().listRecent(null, 500).entries()).hasSize(50);
        assertThat(catalog().listRecent(null, 0).entries()).hasSize(1);
    }

    @Test
    @DisplayName("an empty catalogue answers an empty page without a cursor")
    void empty() {
        assertThat(catalog().listDiscover(null, 20)).isEqualTo(new CatalogPage(List.of(), null));
    }

    private CatalogEntry add(String slug, Instant publishedAt) {
        CatalogEntry entry = new CatalogEntry(UUID.randomUUID(), slug, "https://" + slug + ".example.org/", "a/" + slug,
                "https://github.com/a/" + slug, UUID.randomUUID(), slug, publishedAt);
        store.upsert(entry);
        return entry;
    }

    private static List<String> slugs(CatalogPage page) {
        return page.entries().stream().map(CatalogEntry::slug).toList();
    }
}
