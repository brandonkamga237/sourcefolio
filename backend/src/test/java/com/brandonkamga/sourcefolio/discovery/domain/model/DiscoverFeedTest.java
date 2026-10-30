package com.brandonkamga.sourcefolio.discovery.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import static org.assertj.core.api.Assertions.assertThat;

class DiscoverFeedTest {

    @Test
    @DisplayName("every fifth position goes to a young showcase, the others to older ones")
    void fifthPositionIsReserved() {
        DiscoverFeed.Page page = DiscoverFeed.interleave(list("y", 5), list("o", 20), 0, 10);

        assertThat(page.items()).extracting(Shuffled::key)
                .containsExactly("o0", "o1", "o2", "o3", "y0", "o4", "o5", "o6", "o7", "y1");
        assertThat(page.youngUsed()).isEqualTo(2);
        assertThat(page.oldUsed()).isEqualTo(8);
    }

    @Test
    @DisplayName("the reserved positions follow the feed, not the page: page two starts at position 10")
    void positionsCarryAcrossPages() {
        DiscoverFeed.Page page = DiscoverFeed.interleave(list("y", 5), list("o", 20), 3, 3);

        assertThat(page.items()).extracting(Shuffled::key).containsExactly("o0", "y0", "o1");
    }

    @Test
    @DisplayName("when one list runs out, the other fills its positions")
    void otherListFills() {
        assertThat(DiscoverFeed.interleave(list("y", 3), List.of(), 0, 5).items()).extracting(Shuffled::key)
                .containsExactly("y0", "y1", "y2");
        assertThat(DiscoverFeed.interleave(List.of(), list("o", 6), 0, 6).items()).extracting(Shuffled::key)
                .containsExactly("o0", "o1", "o2", "o3", "o4", "o5");
    }

    @Test
    @DisplayName("an empty catalogue gives an empty page")
    void empty() {
        assertThat(DiscoverFeed.interleave(List.of(), List.of(), 0, 20).items()).isEmpty();
    }

    private static List<Shuffled> list(String prefix, int count) {
        return IntStream.range(0, count).mapToObj(index -> new Shuffled(new CatalogEntry(UUID.randomUUID(), prefix + index,
                "https://" + prefix + index + ".example.org/", "a/b", "https://github.com/a/b", UUID.randomUUID(), "a",
                Instant.parse("2026-10-30T09:00:00Z")), prefix + index)).toList();
    }
}
