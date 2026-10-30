package com.brandonkamga.sourcefolio.engagement.infra.persistence;

import com.brandonkamga.sourcefolio.account.domain.model.User;
import com.brandonkamga.sourcefolio.account.domain.port.UserStore;
import com.brandonkamga.sourcefolio.engagement.domain.port.LikeStore;
import com.brandonkamga.sourcefolio.shared.domain.Language;
import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateReason;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseCandidate;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseSlug;
import com.brandonkamga.sourcefolio.showcase.domain.model.SiteUrl;
import com.brandonkamga.sourcefolio.showcase.domain.port.ShowcaseStore;
import com.brandonkamga.sourcefolio.support.PostgresIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class JpaLikeStoreIT extends PostgresIT {

    private static final Instant NOW = Instant.parse("2026-10-30T09:00:00Z");

    @Autowired
    private LikeStore likes;

    @Autowired
    private ShowcaseStore showcases;

    @Autowired
    private UserStore users;

    @Test
    @DisplayName("a like is one row per member: added once, counted, found, removed once")
    void likeLifecycle() {
        UUID owner = member();
        UUID fan = member();
        UUID other = member();
        UUID showcase = showcases.save(Showcase.publishVerified(owner, new ShowcaseSlug("liked-" + owner.toString().substring(0, 8)),
                new ShowcaseCandidate(new RepositoryCoordinates("o" + owner.toString().substring(0, 8), "portfolio"),
                        new SiteUrl("https://liked.example.org/"), CandidateReason.NAMED_REPOSITORY), NOW)).id().value();

        assertThat(likes.add(fan, showcase, NOW)).isTrue();
        assertThat(likes.add(fan, showcase, NOW)).isFalse();
        assertThat(likes.add(other, showcase, NOW)).isTrue();

        assertThat(likes.count(showcase)).isEqualTo(2);
        assertThat(likes.exists(fan, showcase)).isTrue();
        assertThat(likes.exists(owner, showcase)).isFalse();

        assertThat(likes.remove(fan, showcase)).isTrue();
        assertThat(likes.remove(fan, showcase)).isFalse();
        assertThat(likes.count(showcase)).isEqualTo(1);
    }

    @Test
    @DisplayName("removing all of a member's likes returns the showcases that lost one; counts come grouped in one query")
    void removeAllAndCount() {
        UUID fan = member();
        UUID other = member();
        UUID first = showcase(member());
        UUID second = showcase(member());
        UUID untouched = showcase(member());
        likes.add(fan, first, NOW);
        likes.add(fan, second, NOW);
        likes.add(other, first, NOW);

        assertThat(likes.removeAllBy(fan)).containsExactlyInAnyOrder(first, second);
        assertThat(likes.removeAllBy(fan)).isEmpty();

        assertThat(likes.counts(List.of(first, second, untouched))).isEqualTo(Map.of(first, 1));
        assertThat(likes.counts(List.of())).isEmpty();
    }

    private UUID showcase(UUID owner) {
        String key = owner.toString().substring(0, 8);
        return showcases.save(Showcase.publishVerified(owner, new ShowcaseSlug("s-" + key),
                new ShowcaseCandidate(new RepositoryCoordinates("o" + key, "portfolio"),
                        new SiteUrl("https://" + key + ".example.org/"), CandidateReason.NAMED_REPOSITORY), NOW)).id().value();
    }

    private UUID member() {
        return users.save(User.register(Language.FR, NOW)).id().value();
    }
}
