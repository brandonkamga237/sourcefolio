package com.brandonkamga.sourcefolio.discovery.application;

import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogEntry;
import com.brandonkamga.sourcefolio.discovery.domain.model.MemberCard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class MembersImplTest {

    private static final UUID AUTHOR = UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11");

    private final InMemoryCatalogStore store = new InMemoryCatalogStore();
    private final Members members = new MembersImpl(store);

    @Test
    @DisplayName("a member is found by login in any case, with their profile, newest card, first day and total likes")
    void found() {
        store.upsert(entry("old-site", Instant.parse("2026-09-01T09:00:00Z"), 4));
        store.upsert(entry("new-site", Instant.parse("2026-10-30T09:00:00Z"), 3));
        store.setAuthorProfile(AUTHOR, "CM", List.of("backend", "devops"), "Backend à Douala", "devops");

        MemberCard card = members.findByLogin("NEKWALLA").orElseThrow();

        assertThat(card.login()).isEqualTo("Nekwalla");
        assertThat(card.countryCode()).isEqualTo("CM");
        assertThat(card.specialties()).containsExactly("backend", "devops");
        assertThat(card.role()).isEqualTo("devops");
        assertThat(card.headline()).isEqualTo("Backend à Douala");
        assertThat(card.showcase().slug()).isEqualTo("new-site");
        assertThat(card.onSourceFolioSince()).isEqualTo(Instant.parse("2026-09-01T09:00:00Z"));
        assertThat(card.likesReceived()).isEqualTo(7);
    }

    @Test
    @DisplayName("nobody with a portfolio under that login: nothing")
    void notFound() {
        assertThat(members.findByLogin("ghost")).isEmpty();
    }

    private static CatalogEntry entry(String slug, Instant publishedAt, int likes) {
        return new CatalogEntry(UUID.randomUUID(), slug, "https://" + slug + ".dev/", "nekwalla/" + slug, "https://github.com/nekwalla/" + slug,
                AUTHOR, "Nekwalla", publishedAt, null, null, null, likes);
    }
}
