package com.brandonkamga.sourcefolio.discovery.infra.persistence;

import com.brandonkamga.sourcefolio.account.domain.model.User;
import com.brandonkamga.sourcefolio.account.domain.port.UserStore;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingFacets;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingFilter;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingPage;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingRow;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingSort;
import com.brandonkamga.sourcefolio.discovery.domain.port.RankingStore;
import com.brandonkamga.sourcefolio.shared.domain.Language;
import com.brandonkamga.sourcefolio.support.PostgresIT;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Not @Transactional: the store's writes open their own transactions (REQUIRES_NEW), as they do
// after a commit in production, and must be visible to the reads that follow.
class JpaRankingStoreIT extends PostgresIT {

    private static final Instant NOW = Instant.parse("2026-11-06T10:00:00Z");

    @Autowired
    private RankingStore store;

    @Autowired
    private UserStore users;

    private UUID listed(String login, String country, int activeMonths, int contributions, int stars, int merged, String created,
                        List<String> languages) {
        UUID member = users.save(User.register(Language.FR, NOW)).id().value();
        store.saveIndicators(member, login, null, NOW, Instant.parse(created), contributions, activeMonths,
                List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11), stars, 3, merged, languages);
        store.saveProfile(member, country, "Ville", true);
        return member;
    }

    private static String unique(String login) {
        return login + "-" + UUID.randomUUID().toString().substring(0, 6);
    }

    @Test
    @DisplayName("only published and analysed profiles are listed, sorted on one indicator, filtered by country and language")
    void listing() {
        String country = "ZW";
        String a = unique("a");
        String b = unique("b");
        String c = unique("c");
        listed(a, country, 12, 900, 5, 1, "2019-01-01T00:00:00Z", List.of("PHP", "C++"));
        listed(b, country, 12, 1200, 50, 9, "2015-01-01T00:00:00Z", List.of("Go", "Jupyter Notebook"));
        listed(c, country, 8, 3000, 1, 0, "2021-01-01T00:00:00Z", List.of("PHP"));
        UUID privateOne = users.save(User.register(Language.FR, NOW)).id().value();
        store.saveIndicators(privateOne, unique("p"), null, NOW, NOW, 99999, 12, List.of(), 999, 0, 99, List.of("PHP"));
        store.saveProfile(privateOne, country, null, false);
        UUID notAnalysed = users.save(User.register(Language.FR, NOW)).id().value();
        store.saveProfile(notAnalysed, country, null, true);

        RankingFilter inCountry = new RankingFilter(country, null);
        assertThat(store.page(inCountry, RankingSort.REGULARITY, null, 10).rows()).extracting(RankingRow::login).containsExactly(b, a, c);
        assertThat(store.page(inCountry, RankingSort.CONTRIBUTIONS, null, 10).rows()).extracting(RankingRow::login).containsExactly(c, b, a);
        assertThat(store.page(inCountry, RankingSort.STARS, null, 10).rows()).extracting(RankingRow::login).containsExactly(b, a, c);
        assertThat(store.page(inCountry, RankingSort.EXTERNAL, null, 10).rows()).extracting(RankingRow::login).containsExactly(b, a, c);
        assertThat(store.page(inCountry, RankingSort.SENIORITY, null, 10).rows()).extracting(RankingRow::login).containsExactly(b, a, c);
        assertThat(store.page(new RankingFilter(country, "PHP"), RankingSort.CONTRIBUTIONS, null, 10).rows())
                .extracting(RankingRow::login).containsExactly(c, a);
        assertThat(store.page(new RankingFilter(country, "Jupyter Notebook"), RankingSort.STARS, null, 10).rows())
                .extracting(RankingRow::login).containsExactly(b);

        RankingRow first = store.page(inCountry, RankingSort.REGULARITY, null, 10).rows().getFirst();
        assertThat(first.position()).isEqualTo(1);
        assertThat(first.months()).hasSize(12);
        assertThat(first.languages()).containsExactly("Go", "Jupyter Notebook");
        assertThat(first.countryCode()).isEqualTo(country);
    }

    @Test
    @DisplayName("pages follow each other by keyset, positions continue, and a cursor of another sort is refused")
    void paging() {
        String country = "ZM";
        for (int index = 0; index < 5; index++) {
            listed(unique("m" + index), country, 12, 100 + index, 0, 0, "2020-01-01T00:00:00Z", List.of());
        }
        RankingFilter filter = new RankingFilter(country, null);

        RankingPage first = store.page(filter, RankingSort.CONTRIBUTIONS, null, 2);
        RankingPage second = store.page(filter, RankingSort.CONTRIBUTIONS, first.nextCursor(), 2);
        RankingPage third = store.page(filter, RankingSort.CONTRIBUTIONS, second.nextCursor(), 2);

        assertThat(first.total()).isEqualTo(5);
        assertThat(first.rows()).extracting(RankingRow::publicContributions).containsExactly(104, 103);
        assertThat(second.rows()).extracting(RankingRow::publicContributions).containsExactly(102, 101);
        assertThat(second.rows()).extracting(RankingRow::position).containsExactly(3, 4);
        assertThat(third.rows()).extracting(RankingRow::publicContributions).containsExactly(100);
        assertThat(third.nextCursor()).isNull();
        assertThatThrownBy(() -> store.page(filter, RankingSort.REGULARITY, first.nextCursor(), 2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("facets count listed profiles, each ignoring its own filter")
    void facets() {
        listed(unique("f1"), "MW", 1, 1, 0, 0, "2020-01-01T00:00:00Z", List.of("Rust", "Go"));
        listed(unique("f2"), "MW", 1, 1, 0, 0, "2020-01-01T00:00:00Z", List.of("Rust"));
        listed(unique("f3"), "BW", 1, 1, 0, 0, "2020-01-01T00:00:00Z", List.of("Rust"));

        RankingFacets malawi = store.facets(new RankingFilter("MW", null));
        assertThat(malawi.languages()).contains(new RankingFacets.Count("Rust", 2), new RankingFacets.Count("Go", 1));
        assertThat(malawi.countries()).contains(new RankingFacets.Count("MW", 2), new RankingFacets.Count("BW", 1));
    }
}
