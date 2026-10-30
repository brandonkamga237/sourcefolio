package com.brandonkamga.sourcefolio.showcase.application;

import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateReason;
import com.brandonkamga.sourcefolio.showcase.domain.model.PublicRepository;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseCandidate;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseSlug;
import com.brandonkamga.sourcefolio.showcase.domain.model.SiteUrl;
import com.brandonkamga.sourcefolio.showcase.domain.port.RepositoryHost;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class RepositoryStarsImplTest {

    private static final Instant NOW = Instant.parse("2026-10-30T09:00:00Z");

    private final ShowcasePublicationImplTest.InMemoryShowcaseStore showcases = new ShowcasePublicationImplTest.InMemoryShowcaseStore();
    private final Map<String, Integer> github = new HashMap<>();
    private final List<String> read = new ArrayList<>();
    private final List<ShowcaseStarsChanged> announced = new ArrayList<>();
    private final RepositoryStars stars = new RepositoryStarsImpl(showcases, new RepositoryHost() {
        @Override
        public List<PublicRepository> listPublicRepositories(String login) {
            throw new AssertionError("stars list no repositories");
        }

        @Override
        public OptionalInt stars(RepositoryCoordinates repository) {
            read.add(repository.toString());
            Integer count = github.get(repository.toString());
            return count == null ? OptionalInt.empty() : OptionalInt.of(count);
        }
    }, new ShowcaseEvents() {
        @Override
        public void showcasePublished(ShowcasePublished event) {
            throw new AssertionError("stars publish nothing");
        }

        @Override
        public void previewChanged(ShowcasePreviewChanged event) {
            throw new AssertionError("stars change no preview");
        }

        @Override
        public void showcaseRemoved(ShowcaseRemoved event) {
            throw new AssertionError("stars remove nothing");
        }

        @Override
        public void starsChanged(ShowcaseStarsChanged event) {
            announced.add(event);
        }
    }, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    @DisplayName("the stars are read, stored with the time, and the new count announced")
    void refresh() {
        Showcase showcase = showcases.save(published("nekwalla"));
        github.put("nekwalla/nekwalla.github.io", 12);

        stars.refresh(showcase.id().value());

        assertThat(showcases.rows.get(showcase.id().value()).sourceRepo().stars()).isEqualTo(12);
        assertThat(showcases.rows.get(showcase.id().value()).sourceRepo().starsCheckedAt()).isEqualTo(NOW);
        assertThat(announced).containsExactly(new ShowcaseStarsChanged(showcase.id().value(), 12));
    }

    @Test
    @DisplayName("an unchanged count is stored again (fresh for a day) without announcing anything")
    void unchanged() {
        Showcase showcase = showcases.save(published("nekwalla"));
        github.put("nekwalla/nekwalla.github.io", 12);
        stars.refresh(showcase.id().value());
        announced.clear();

        stars.refresh(showcase.id().value());

        assertThat(announced).isEmpty();
    }

    @Test
    @DisplayName("a repository gone private or deleted loses its count")
    void repositoryGone() {
        Showcase showcase = showcases.save(published("nekwalla"));
        github.put("nekwalla/nekwalla.github.io", 12);
        stars.refresh(showcase.id().value());
        github.clear();

        stars.refresh(showcase.id().value());

        assertThat(showcases.rows.get(showcase.id().value()).sourceRepo().stars()).isNull();
        assertThat(announced).extracting(ShowcaseStarsChanged::stars).containsExactly(12, null);
    }

    @Test
    @DisplayName("a showcase removed meanwhile is skipped without calling GitHub")
    void removedMeanwhile() {
        stars.refresh(UUID.fromString("7c4d2a10-0000-4000-8000-000000000001"));

        assertThat(read).isEmpty();
    }

    @Test
    @DisplayName("due: never read first, then read more than a day ago; read today is not due")
    void due() {
        Showcase fresh = showcases.save(published("fresh"));
        Showcase stale = showcases.save(published("stale"));
        Showcase never = showcases.save(published("never"));
        showcases.saveRepositoryStars(fresh.id(), 1, NOW.minus(Duration.ofHours(2)));
        showcases.saveRepositoryStars(stale.id(), 1, NOW.minus(Duration.ofHours(30)));

        assertThat(stars.listDue(10)).containsExactly(never.id().value(), stale.id().value());
        assertThat(stars.listDue(1)).containsExactly(never.id().value());
    }

    private static Showcase published(String login) {
        return Showcase.publishVerified(UUID.randomUUID(), new ShowcaseSlug(login), new ShowcaseCandidate(
                new RepositoryCoordinates(login, login + ".github.io"), new SiteUrl("https://" + login + ".github.io/"),
                CandidateReason.USER_PAGES), NOW);
    }
}
