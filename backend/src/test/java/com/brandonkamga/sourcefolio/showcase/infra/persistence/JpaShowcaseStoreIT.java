package com.brandonkamga.sourcefolio.showcase.infra.persistence;

import com.brandonkamga.sourcefolio.account.domain.model.User;
import com.brandonkamga.sourcefolio.account.domain.port.UserStore;
import com.brandonkamga.sourcefolio.shared.domain.Language;
import com.brandonkamga.sourcefolio.shared.exception.AppException;
import com.brandonkamga.sourcefolio.shared.exception.ConflictException;
import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateReason;
import com.brandonkamga.sourcefolio.showcase.domain.model.PreviewMedia;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryProvider;
import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseCandidate;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseId;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseSlug;
import com.brandonkamga.sourcefolio.showcase.domain.model.SiteUrl;
import com.brandonkamga.sourcefolio.showcase.domain.port.ShowcaseStore;
import com.brandonkamga.sourcefolio.support.PostgresIT;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class JpaShowcaseStoreIT extends PostgresIT {

    private static final Instant NOW = Instant.parse("2026-10-30T09:00:00Z");

    @Autowired
    private ShowcaseStore showcases;

    @Autowired
    private UserStore users;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("a published showcase is stored with its repository and read back as it was")
    void roundTrip() {
        UUID member = member();
        Showcase published = Showcase.publishVerified(member, slug("nekwalla.github.io"), candidate("nekwalla", "nekwalla.github.io"), NOW);

        Showcase stored = showcases.save(published);
        entityManager.flush();
        entityManager.clear();

        assertThat(stored.pendingEvents()).isEmpty();
        assertThat(showcases.findByOwner(member)).contains(stored);
        assertThat(showcases.findBySlug(stored.slug())).contains(stored);
        assertThat(showcases.existsBySlug(stored.slug())).isTrue();
        assertThat(showcases.existsBySlug(new ShowcaseSlug("nobody"))).isFalse();
    }

    @Test
    @DisplayName("the chosen preview is stored and read back, and can be removed")
    void preview() {
        UUID member = member();
        Showcase stored = showcases.save(Showcase.publishVerified(member, slug("previewed"), candidate("nekwalla", "previewed"), NOW));
        entityManager.flush();

        Showcase withPreview = showcases.save(stored
                .withPreviewImage(new PreviewMedia("previews/a.webp", "https://media.test/previews/a.webp"))
                .withPreviewVideo(new PreviewMedia("previews/b.mp4", "https://media.test/previews/b.mp4")));
        entityManager.flush();
        entityManager.clear();
        assertThat(showcases.findByOwner(member)).contains(withPreview);

        showcases.save(withPreview.withPreviewVideo(null));
        entityManager.flush();
        entityManager.clear();
        assertThat(showcases.findByOwner(member)).hasValueSatisfying(found -> {
            assertThat(found.previewImage()).isNotNull();
            assertThat(found.previewVideo()).isNull();
        });
    }

    @Test
    @DisplayName("the first status is written to the history")
    void historyWritten() {
        Showcase stored = showcases.save(Showcase.publishVerified(member(), slug("portfolio"), candidate("nekwalla", "portfolio"), NOW));
        entityManager.flush();

        List<?> rows = entityManager.createNativeQuery(
                        "SELECT from_status, to_status, reason, actor FROM showcase_status_events WHERE showcase_id = :id")
                .setParameter("id", stored.id().value())
                .getResultList();
        assertThat(rows).hasSize(1);
        assertThat((Object[]) rows.getFirst()).containsExactly(null, "PUBLISHED", "AUTO_VERIFIED", "SYSTEM");
    }

    @Test
    @DisplayName("a repository is found whatever the case it is written in")
    void existsIgnoresCase() {
        showcases.save(Showcase.publishVerified(member(), slug("Portfolio"), candidate("Nekwalla", "Portfolio"), NOW));
        entityManager.flush();

        assertThat(showcases.existsByRepository(RepositoryProvider.GITHUB, new RepositoryCoordinates("nekwalla", "portfolio"))).isTrue();
        assertThat(showcases.existsByRepository(RepositoryProvider.GITHUB, new RepositoryCoordinates("nekwalla", "site"))).isFalse();
    }

    @Test
    @DisplayName("the same repository cannot be behind two showcases, even written in another case")
    void repositoryUnique() {
        showcases.save(Showcase.publishVerified(member(), slug("portfolio"), candidate("nekwalla", "portfolio"), NOW));
        entityManager.flush();

        assertThatThrownBy(() -> showcases.save(Showcase.publishVerified(member(), slug("portfolio"), candidate("NEKWALLA", "portfolio"), NOW)))
                .isInstanceOf(ConflictException.class)
                .extracting(thrown -> ((AppException) thrown).messageKey())
                .isEqualTo("showcase.repository-taken");
    }

    @Test
    @DisplayName("two showcases cannot share a public address")
    void slugUnique() {
        showcases.save(Showcase.publishVerified(member(), new ShowcaseSlug("nekwalla"), candidate("nekwalla", "portfolio"), NOW));
        entityManager.flush();

        assertThatThrownBy(() -> {
            showcases.save(Showcase.publishVerified(member(), new ShowcaseSlug("nekwalla"), candidate("other", "site"), NOW));
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("deleting a member's showcases returns them as they were and frees their repository; others stay")
    void deleteAllByOwner() {
        UUID member = member();
        UUID other = member();
        Showcase mine = showcases.save(Showcase.publishVerified(member, slug("deleted"), candidate("nekwalla", "deleted"), NOW)
                .withPreviewImage(new PreviewMedia("previews/c.webp", "https://media.test/previews/c.webp")));
        Showcase theirs = showcases.save(Showcase.publishVerified(other, slug("kept"), candidate("ayo", "kept"), NOW));
        entityManager.flush();
        entityManager.clear();

        List<Showcase> removed = showcases.deleteAllByOwner(member);
        entityManager.flush();
        entityManager.clear();

        assertThat(removed).containsExactly(mine.stored());
        assertThat(showcases.findByOwner(member)).isEmpty();
        assertThat(showcases.existsByRepository(RepositoryProvider.GITHUB, new RepositoryCoordinates("nekwalla", "deleted"))).isFalse();
        assertThat(showcases.findByOwner(other)).contains(theirs);
        assertThat(showcases.deleteAllByOwner(member)).isEmpty();
    }

    @Test
    @DisplayName("stars are written alone, read back, and due once more than the cut-off old; never read ones come first")
    void repositoryStars() {
        Showcase never = showcases.save(Showcase.publishVerified(member(), slug("never-read"), candidate("nekwalla", "never-read"), NOW));
        Showcase stale = showcases.save(Showcase.publishVerified(member(), slug("stale-read"), candidate("nekwalla", "stale-read"), NOW));
        Showcase fresh = showcases.save(Showcase.publishVerified(member(), slug("fresh-read"), candidate("nekwalla", "fresh-read"), NOW));
        entityManager.flush();

        showcases.saveRepositoryStars(stale.id(), 3, NOW.minusSeconds(30 * 3600));
        showcases.saveRepositoryStars(fresh.id(), 9, NOW.minusSeconds(3600));
        entityManager.flush();
        entityManager.clear();

        assertThat(showcases.findById(fresh.id())).hasValueSatisfying(found -> {
            assertThat(found.sourceRepo().stars()).isEqualTo(9);
            assertThat(found.sourceRepo().starsCheckedAt()).isEqualTo(NOW.minusSeconds(3600));
            assertThat(found.siteUrl()).isEqualTo(fresh.siteUrl());
        });
        List<ShowcaseId> due = showcases.findDueForStars(NOW.minusSeconds(24 * 3600), 1000);
        assertThat(due).contains(never.id(), stale.id()).doesNotContain(fresh.id());
        assertThat(due.indexOf(never.id())).isLessThan(due.indexOf(stale.id()));
    }

    @Test
    @DisplayName("a member who never published has no showcase")
    void noShowcase() {
        assertThat(showcases.findByOwner(member())).isEmpty();
    }

    private UUID member() {
        return users.save(User.register(Language.FR, NOW)).id().value();
    }

    // One address per test showcase: the repository name is unique within each test.
    private static ShowcaseSlug slug(String repositoryName) {
        return new ShowcaseSlug(repositoryName.toLowerCase().replace(".", "-") + "-" + UUID.randomUUID().toString().substring(0, 8));
    }

    private static ShowcaseCandidate candidate(String owner, String name) {
        return new ShowcaseCandidate(new RepositoryCoordinates(owner, name), new SiteUrl("https://" + name.toLowerCase() + ".example.org/"),
                CandidateReason.NAMED_REPOSITORY);
    }
}
