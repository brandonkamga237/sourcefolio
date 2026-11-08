package com.brandonkamga.sourcefolio.discovery.application;

import com.brandonkamga.sourcefolio.discovery.domain.model.CityCount;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingFacets;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingFilter;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingPage;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingSort;
import com.brandonkamga.sourcefolio.discovery.domain.port.RankingStore;
import com.brandonkamga.sourcefolio.shared.exception.AppException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RankingsImplTest {

    private final List<Object[]> asked = new ArrayList<>();
    private final RankingStore store = new RankingStore() {
        @Override
        public void saveIndicators(UUID memberId, String login, String name, Instant analyzedAt, Instant accountCreatedAt,
                                   int publicContributions, int activeMonths, List<Integer> months, int stars, int followers,
                                   int mergedPullRequestsElsewhere, List<String> languages) {
        }

        @Override
        public void saveProfile(UUID memberId, String countryCode, String city, boolean published) {
        }

        @Override
        public RankingPage page(RankingFilter filter, RankingSort sort, String cursor, int size) {
            if ("broken".equals(cursor)) {
                throw new IllegalArgumentException("Unreadable ranking cursor");
            }
            asked.add(new Object[] {filter, sort, cursor, size});
            return new RankingPage(List.of(), null, 0);
        }

        @Override
        public RankingFacets facets(RankingFilter filter) {
            asked.add(new Object[] {filter});
            return new RankingFacets(List.of(), List.of());
        }

        @Override
        public List<CityCount> cityCounts() {
            return List.of();
        }
    };
    private final RankingsImpl rankings = new RankingsImpl(store);

    @Test
    @DisplayName("regularity is the default sort; filters are cleaned and the page size kept within bounds")
    void defaults() {
        rankings.listRanking(null, " cm ", " PHP ", "", 500);

        assertThat(asked.getFirst()).containsExactly(new RankingFilter("CM", "PHP"), RankingSort.REGULARITY, null, 100);
    }

    @Test
    @DisplayName("a sort is read without case")
    void sortNames() {
        rankings.listRanking("Stars", null, null, null, 0);

        assertThat(asked.getFirst()).containsExactly(new RankingFilter(null, null), RankingSort.STARS, null, 1);
    }

    @Test
    @DisplayName("an unknown sort, a malformed country or an unreadable cursor is a 400")
    void invalid() {
        assertThatThrownBy(() -> rankings.listRanking("likes", null, null, null, 50))
                .isInstanceOfSatisfying(AppException.class, error -> assertThat(error.messageKey()).isEqualTo("ranking.sort-invalid"));
        assertThatThrownBy(() -> rankings.listRanking(null, "Cameroun", null, null, 50))
                .isInstanceOfSatisfying(AppException.class, error -> assertThat(error.messageKey()).isEqualTo("ranking.country-invalid"));
        assertThatThrownBy(() -> rankings.listRanking(null, null, null, "broken", 50))
                .isInstanceOfSatisfying(AppException.class, error -> assertThat(error.messageKey()).isEqualTo("ranking.cursor-invalid"));
    }
}
