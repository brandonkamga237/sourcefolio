package com.brandonkamga.sourcefolio.engagement.infra.persistence;

import com.brandonkamga.sourcefolio.account.domain.model.User;
import com.brandonkamga.sourcefolio.account.domain.port.UserStore;
import com.brandonkamga.sourcefolio.engagement.domain.model.Feedback;
import com.brandonkamga.sourcefolio.engagement.domain.model.FeedbackAngle;
import com.brandonkamga.sourcefolio.engagement.domain.model.FeedbackBody;
import com.brandonkamga.sourcefolio.engagement.domain.model.FeedbackCursor;
import com.brandonkamga.sourcefolio.engagement.domain.port.FeedbackReadStore;
import com.brandonkamga.sourcefolio.engagement.domain.port.FeedbackStore;
import com.brandonkamga.sourcefolio.shared.domain.Language;
import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateReason;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseCandidate;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseSlug;
import com.brandonkamga.sourcefolio.showcase.domain.model.SiteUrl;
import com.brandonkamga.sourcefolio.showcase.domain.port.ShowcaseStore;
import com.brandonkamga.sourcefolio.support.PostgresIT;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class JpaFeedbackStoreIT extends PostgresIT {

    private static final Instant NOW = Instant.parse("2026-11-01T09:00:00Z");

    @Autowired
    private FeedbackStore feedback;

    @Autowired
    private ShowcaseStore showcases;

    @Autowired
    private UserStore users;

    @Autowired
    private FeedbackReadStore reads;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("feedback is stored, read back field for field, newest first and page by page, counted and renamed")
    void lifecycle() {
        UUID showcase = showcase(member());
        UUID author = member();
        Feedback first = write(showcase, author, FeedbackAngle.MOBILE, "Le menu déborde sur mon téléphone.", NOW);
        Feedback second = write(showcase, author, null, "Très clair, j’ai compris en 10 secondes.", NOW.plusSeconds(60));
        Feedback third = write(showcase, author, FeedbackAngle.CODE, "Le README mériterait une section setup.", NOW.plusSeconds(120));
        List.of(first, second, third).forEach(feedback::add);
        entityManager.flush();
        entityManager.clear();

        List<Feedback> page = feedback.page(showcase, null, 2);
        assertThat(page).containsExactly(third, second);
        assertThat(feedback.page(showcase, new FeedbackCursor(second.createdAt(), second.id()), 2)).containsExactly(first);
        assertThat(feedback.count(showcase)).isEqualTo(3);
        assertThat(feedback.find(first.id())).contains(first);

        feedback.renameAuthor(author, "renamed-dev");
        feedback.delete(second.id());
        entityManager.flush();
        entityManager.clear();
        assertThat(feedback.page(showcase, null, 10)).extracting(Feedback::authorLogin).containsOnly("renamed-dev");
        assertThat(feedback.count(showcase)).isEqualTo(2);
    }

    @Test
    @DisplayName("removing a member's feedback answers each showcase it was on once, and leaves the others' feedback")
    void removeAllBy() {
        UUID mine = showcase(member());
        UUID theirs = showcase(member());
        UUID leaving = member();
        UUID staying = member();
        List.of(write(mine, leaving, null, "Le menu déborde sur mon téléphone.", NOW),
                write(mine, leaving, null, "Et le footer aussi, en paysage.", NOW.plusSeconds(1)),
                write(theirs, leaving, null, "Très clair, j’ai compris en 10 secondes.", NOW),
                write(mine, staying, null, "Le README mériterait une section setup.", NOW)).forEach(feedback::add);
        entityManager.flush();

        assertThat(feedback.removeAllBy(leaving)).containsExactlyInAnyOrder(mine, theirs);
        entityManager.flush();
        entityManager.clear();
        assertThat(feedback.count(mine)).isEqualTo(1);
        assertThat(feedback.count(theirs)).isZero();
    }

    @Test
    @DisplayName("feedback written after an instant is counted in SQL, and an author's last read is stored and replaced")
    void unreadAndReads() {
        UUID owner = member();
        UUID showcase = showcase(owner);
        UUID author = member();
        List.of(write(showcase, author, null, "Le menu déborde sur mon téléphone.", NOW),
                write(showcase, author, null, "Et le footer aussi, en paysage.", NOW.plusSeconds(60))).forEach(feedback::add);
        reads.markSeen(owner, NOW.plusSeconds(1));
        entityManager.flush();
        reads.markSeen(owner, NOW.plusSeconds(30));
        entityManager.flush();
        entityManager.clear();

        assertThat(feedback.countSince(showcase, null)).isEqualTo(2);
        assertThat(feedback.countSince(showcase, NOW.plusSeconds(30))).isEqualTo(1);
        assertThat(reads.seenAt(owner)).contains(NOW.plusSeconds(30));
        assertThat(reads.seenAt(author)).isEmpty();
    }

    private static Feedback write(UUID showcase, UUID author, FeedbackAngle angle, String body, Instant at) {
        return new Feedback(UUID.randomUUID(), showcase, author, "author-dev", angle, FeedbackBody.parse(body), Language.FR, at);
    }

    private UUID member() {
        return users.save(User.register(Language.FR, NOW)).id().value();
    }

    private UUID showcase(UUID owner) {
        String key = owner.toString().substring(0, 8);
        return showcases.save(Showcase.publishVerified(owner, new ShowcaseSlug("fb-" + key), new ShowcaseCandidate(
                new RepositoryCoordinates("o" + key, "portfolio"), new SiteUrl("https://" + key + ".example.org/"),
                CandidateReason.NAMED_REPOSITORY), NOW)).id().value();
    }
}
