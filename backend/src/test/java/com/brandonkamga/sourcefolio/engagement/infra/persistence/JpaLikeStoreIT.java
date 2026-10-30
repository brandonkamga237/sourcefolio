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

    private UUID member() {
        return users.save(User.register(Language.FR, NOW)).id().value();
    }
}
