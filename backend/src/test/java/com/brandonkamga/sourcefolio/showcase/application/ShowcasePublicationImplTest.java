package com.brandonkamga.sourcefolio.showcase.application;

import com.brandonkamga.sourcefolio.shared.exception.AppException;
import com.brandonkamga.sourcefolio.shared.exception.ConflictException;
import com.brandonkamga.sourcefolio.shared.exception.ErrorCode;
import com.brandonkamga.sourcefolio.showcase.application.ShowcaseDetectionImplTest.InMemoryCandidateStore;
import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateReason;
import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateScan;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryProvider;
import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseCandidate;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseSlug;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseStatus;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseStatusEvent;
import com.brandonkamga.sourcefolio.showcase.domain.model.SiteUrl;
import com.brandonkamga.sourcefolio.showcase.domain.model.StatusActor;
import com.brandonkamga.sourcefolio.showcase.domain.port.ShowcaseStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShowcasePublicationImplTest {

    private static final Instant NOW = Instant.parse("2026-10-30T09:00:00Z");
    private static final UUID MEMBER = UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11");
    private static final UUID OTHER_MEMBER = UUID.fromString("5d1e7a40-2b9c-4c55-8f0e-6a3b2c1d0e99");
    private static final ShowcaseCandidate PAGES = candidate("nekwalla.github.io", "https://nekwalla.github.io/", CandidateReason.USER_PAGES);
    private static final ShowcaseCandidate API = candidate("api", "https://api.example.org/", CandidateReason.HOMEPAGE);

    private final InMemoryShowcaseStore showcases = new InMemoryShowcaseStore();
    private final InMemoryCandidateStore candidates = new InMemoryCandidateStore();
    private final Set<String> reachableSites = new HashSet<>();
    private final List<String> fetched = new ArrayList<>();
    private final List<ShowcasePublished> published = new ArrayList<>();
    private final Map<UUID, String> logins = new HashMap<>(Map.of(MEMBER, "Nekwalla", OTHER_MEMBER, "ayo-dev"));
    private final ShowcasePublication publication = new ShowcasePublicationImpl(showcases, candidates, url -> {
        fetched.add(url);
        return reachableSites.contains(url);
    }, logins::get, new ShowcaseEvents() {
        @Override
        public void showcasePublished(ShowcasePublished event) {
            published.add(event);
        }

        @Override
        public void previewChanged(ShowcasePreviewChanged event) {
            throw new AssertionError("publishing changes no preview");
        }

        @Override
        public void showcaseRemoved(ShowcaseRemoved event) {
            throw new AssertionError("publishing removes nothing");
        }
    }, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    @DisplayName("a detected candidate whose site answers is published at once, with its repository and its first history line")
    void publishesVerifiedCandidate() {
        candidates.replace(MEMBER, new CandidateScan(NOW, List.of(PAGES, API)));
        reachableSites.add("https://nekwalla.github.io/");

        Showcase published = publication.publish(new PublishShowcase(MEMBER, new RepositoryCoordinates("nekwalla", "nekwalla.github.io")));

        Showcase stored = showcases.findByOwner(MEMBER).orElseThrow();
        assertThat(stored).isEqualTo(published);
        assertThat(stored.status()).isEqualTo(ShowcaseStatus.PUBLISHED);
        assertThat(stored.publishedAt()).isEqualTo(NOW);
        assertThat(stored.siteUrl().value()).isEqualTo("https://nekwalla.github.io/");
        assertThat(stored.sourceRepo().url()).isEqualTo("https://github.com/nekwalla/nekwalla.github.io");
        assertThat(showcases.history.get(stored.id().value())).containsExactly(
                new ShowcaseStatusEvent(null, ShowcaseStatus.PUBLISHED, ShowcaseStatusEvent.AUTO_VERIFIED, StatusActor.SYSTEM, NOW));
        assertThat(this.published).containsExactly(new ShowcasePublished(stored.id().value(), "nekwalla",
                "https://nekwalla.github.io/", "nekwalla/nekwalla.github.io", "https://github.com/nekwalla/nekwalla.github.io",
                MEMBER, "Nekwalla", NOW));
    }

    @Test
    @DisplayName("the public address comes from the member's login, lowercased")
    void slugFromLogin() {
        candidates.replace(MEMBER, new CandidateScan(NOW, List.of(PAGES)));
        reachableSites.add("https://nekwalla.github.io/");

        Showcase published = publication.publish(new PublishShowcase(MEMBER, PAGES.repository()));

        assertThat(published.slug()).isEqualTo(new ShowcaseSlug("nekwalla"));
    }

    @Test
    @DisplayName("a login already used as an address by an earlier member gets the next free suffix")
    void slugCollision() {
        candidates.replace(OTHER_MEMBER, new CandidateScan(NOW, List.of(API)));
        candidates.replace(MEMBER, new CandidateScan(NOW, List.of(PAGES)));
        reachableSites.addAll(List.of("https://nekwalla.github.io/", "https://api.example.org/"));
        // The other member published as "nekwalla" before renaming on GitHub.
        logins.put(OTHER_MEMBER, "nekwalla");
        publication.publish(new PublishShowcase(OTHER_MEMBER, API.repository()));

        Showcase published = publication.publish(new PublishShowcase(MEMBER, PAGES.repository()));

        assertThat(published.slug()).isEqualTo(new ShowcaseSlug("nekwalla-2"));
    }

    @Test
    @DisplayName("the member may name the repository in any case, as GitHub does")
    void repositoryCaseDoesNotMatter() {
        candidates.replace(MEMBER, new CandidateScan(NOW, List.of(API)));
        reachableSites.add("https://api.example.org/");

        publication.publish(new PublishShowcase(MEMBER, new RepositoryCoordinates("Nekwalla", "API")));

        assertThat(showcases.findByOwner(MEMBER)).isPresent();
    }

    @Test
    @DisplayName("a site that does not answer is not published, and nothing is stored")
    void unreachableSiteIsRefused() {
        candidates.replace(MEMBER, new CandidateScan(NOW, List.of(PAGES)));

        assertThatThrownBy(() -> publication.publish(new PublishShowcase(MEMBER, PAGES.repository())))
                .isInstanceOf(ConflictException.class)
                .extracting(thrown -> ((AppException) thrown).messageKey())
                .isEqualTo("showcase.site-unreachable");
        assertThat(showcases.rows).isEmpty();
        assertThat(published).isEmpty();
    }

    @Test
    @DisplayName("a repository the detection did not find for this member cannot be published from here")
    void onlyOwnCandidates() {
        candidates.replace(OTHER_MEMBER, new CandidateScan(NOW, List.of(PAGES)));
        reachableSites.add("https://nekwalla.github.io/");

        assertThatThrownBy(() -> publication.publish(new PublishShowcase(MEMBER, PAGES.repository())))
                .isInstanceOf(AppException.class)
                .extracting(thrown -> ((AppException) thrown).code())
                .isEqualTo(ErrorCode.NOT_FOUND);
        assertThat(fetched).isEmpty();
    }

    @Test
    @DisplayName("one published portfolio per member: a second publication is refused before any site is fetched")
    void oneShowcasePerMember() {
        candidates.replace(MEMBER, new CandidateScan(NOW, List.of(PAGES, API)));
        reachableSites.addAll(List.of("https://nekwalla.github.io/", "https://api.example.org/"));
        publication.publish(new PublishShowcase(MEMBER, PAGES.repository()));
        fetched.clear();

        assertThatThrownBy(() -> publication.publish(new PublishShowcase(MEMBER, API.repository())))
                .isInstanceOf(ConflictException.class)
                .extracting(thrown -> ((AppException) thrown).messageKey())
                .isEqualTo("showcase.already-published");
        assertThat(fetched).isEmpty();
        assertThat(showcases.rows).hasSize(1);
    }

    @Test
    @DisplayName("a repository already behind someone's showcase is not published twice")
    void repositoryPublishedOnce() {
        candidates.replace(OTHER_MEMBER, new CandidateScan(NOW, List.of(PAGES)));
        candidates.replace(MEMBER, new CandidateScan(NOW, List.of(PAGES)));
        reachableSites.add("https://nekwalla.github.io/");
        publication.publish(new PublishShowcase(OTHER_MEMBER, PAGES.repository()));

        assertThatThrownBy(() -> publication.publish(new PublishShowcase(MEMBER, PAGES.repository())))
                .isInstanceOf(ConflictException.class)
                .extracting(thrown -> ((AppException) thrown).messageKey())
                .isEqualTo("showcase.repository-taken");
    }

    @Test
    @DisplayName("before the first publication, the member has no showcase")
    void noShowcaseYet() {
        assertThat(publication.findMyShowcase(MEMBER)).isEmpty();
    }

    private static ShowcaseCandidate candidate(String name, String site, CandidateReason reason) {
        return new ShowcaseCandidate(new RepositoryCoordinates("nekwalla", name), new SiteUrl(site), reason);
    }

    /** Copies like a database: what the use case forgets to save is not there. */
    static class InMemoryShowcaseStore implements ShowcaseStore {
        final Map<UUID, Showcase> rows = new HashMap<>();
        final Map<UUID, List<ShowcaseStatusEvent>> history = new HashMap<>();

        @Override
        public Showcase save(Showcase showcase) {
            UUID id = showcase.id().value();
            boolean taken = rows.values().stream().anyMatch(row -> !row.id().equals(showcase.id())
                    && row.sourceRepo().coordinates().sameAs(showcase.sourceRepo().coordinates()));
            if (taken) {
                throw new ConflictException("showcase.repository-taken");
            }
            rows.put(id, showcase.stored());
            history.computeIfAbsent(id, key -> new ArrayList<>()).addAll(showcase.pendingEvents());
            return showcase.stored();
        }

        @Override
        public Optional<Showcase> findByOwner(UUID ownerId) {
            return rows.values().stream().filter(row -> row.ownerId().equals(ownerId))
                    .max(Comparator.comparing(Showcase::createdAt));
        }

        @Override
        public Optional<Showcase> findBySlug(ShowcaseSlug slug) {
            return rows.values().stream().filter(row -> row.slug().equals(slug)).findFirst();
        }

        @Override
        public boolean existsBySlug(ShowcaseSlug slug) {
            return findBySlug(slug).isPresent();
        }

        @Override
        public boolean existsByRepository(RepositoryProvider provider, RepositoryCoordinates repository) {
            return rows.values().stream().anyMatch(row -> row.sourceRepo().provider() == provider
                    && row.sourceRepo().coordinates().sameAs(repository));
        }

        @Override
        public List<Showcase> deleteAllByOwner(UUID ownerId) {
            List<Showcase> removed = rows.values().stream().filter(row -> row.ownerId().equals(ownerId)).toList();
            removed.forEach(row -> {
                rows.remove(row.id().value());
                history.remove(row.id().value());
            });
            return removed;
        }
    }
}
