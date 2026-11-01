package com.brandonkamga.sourcefolio.discovery.application;

import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogEntry;
import com.brandonkamga.sourcefolio.profile.application.PublicProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class CatalogProjectionImplTest {

    private static final UUID AUTHOR = UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11");
    private static final UUID NEWCOMER = UUID.fromString("5d1e7a40-2b9c-4c55-8f0e-6a3b2c1d0e99");

    private final InMemoryCatalogStore store = new InMemoryCatalogStore();
    private final Map<UUID, PublicProfile> profiles = Map.of(AUTHOR, new PublicProfile("CM", List.of("backend", "devops"), "Backend à Douala", "devops"));
    private final CatalogProjection projection = new CatalogProjectionImpl(store, memberId -> Optional.ofNullable(profiles.get(memberId)));

    @Test
    @DisplayName("a new card starts with the profile its author already answered")
    void newCardTakesTheProfile() {
        projection.add(entry(AUTHOR, "nekwalla"));

        assertThat(store.rows.values()).singleElement().satisfies(card -> {
            assertThat(card.countryCode()).isEqualTo("CM");
            assertThat(card.specialties()).containsExactly("backend", "devops");
            assertThat(card.headline()).isEqualTo("Backend à Douala");
        });
    }

    @Test
    @DisplayName("an author who answered nothing gets a card without country or specialties")
    void newCardWithoutProfile() {
        projection.add(entry(NEWCOMER, "ayo-dev"));

        assertThat(store.rows.values()).singleElement().satisfies(card -> {
            assertThat(card.countryCode()).isNull();
            assertThat(card.specialties()).isEmpty();
        });
    }

    @Test
    @DisplayName("a profile change reaches the cards of that author only")
    void profileChange() {
        projection.add(entry(AUTHOR, "nekwalla"));
        projection.add(entry(NEWCOMER, "ayo-dev"));

        projection.updateAuthorProfile(NEWCOMER, "NG", List.of("mobile"), null, "mobile");

        assertThat(store.rows.values()).extracting(CatalogEntry::slug, CatalogEntry::countryCode)
                .containsExactlyInAnyOrder(org.assertj.core.groups.Tuple.tuple("nekwalla", "CM"), org.assertj.core.groups.Tuple.tuple("ayo-dev", "NG"));
    }

    private static CatalogEntry entry(UUID owner, String slug) {
        return new CatalogEntry(UUID.randomUUID(), slug, "https://" + slug + ".github.io/", slug + "/portfolio",
                "https://github.com/" + slug + "/portfolio", owner, slug, Instant.parse("2026-10-30T09:00:00Z"));
    }
}
