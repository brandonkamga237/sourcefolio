package com.brandonkamga.sourcefolio.discovery.infra.events;

import com.brandonkamga.sourcefolio.account.application.MemberSignedIn;
import com.brandonkamga.sourcefolio.discovery.application.CatalogProjection;
import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogEntry;
import com.brandonkamga.sourcefolio.engagement.application.ShowcaseFeedbackChanged;
import com.brandonkamga.sourcefolio.engagement.application.ShowcaseLikesChanged;
import com.brandonkamga.sourcefolio.profile.application.ProfileChanged;
import com.brandonkamga.sourcefolio.showcase.application.ShowcasePreviewChanged;
import com.brandonkamga.sourcefolio.showcase.application.ShowcasePublished;
import com.brandonkamga.sourcefolio.showcase.application.ShowcaseRepositoryChanged;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class CatalogListenersTest {

    private static final UUID SHOWCASE = UUID.fromString("7c4d2a10-0000-4000-8000-000000000001");
    private static final UUID MEMBER = UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11");
    private static final ShowcasePublished PUBLISHED = new ShowcasePublished(SHOWCASE, "nekwalla", "https://nekwalla.github.io/",
            "nekwalla/nekwalla.github.io", "https://github.com/nekwalla/nekwalla.github.io", MEMBER, "Nekwalla",
            Instant.parse("2026-10-30T09:00:00Z"));

    private final List<Object> calls = new ArrayList<>();

    @Test
    @DisplayName("a published showcase becomes a catalogue entry with the same fields")
    void publishedBecomesEntry() {
        new CatalogListeners(recording()).on(PUBLISHED);

        assertThat(calls).containsExactly(new CatalogEntry(SHOWCASE, "nekwalla", "https://nekwalla.github.io/",
                "nekwalla/nekwalla.github.io", "https://github.com/nekwalla/nekwalla.github.io", MEMBER, "Nekwalla",
                Instant.parse("2026-10-30T09:00:00Z")));
    }

    @Test
    @DisplayName("a sign-in carries the member's current login to their cards")
    void signInRenames() {
        new CatalogListeners(recording()).on(new MemberSignedIn(MEMBER, "nekwalla-dev"));

        assertThat(calls).containsExactly(MEMBER + " nekwalla-dev");
    }

    @Test
    @DisplayName("a new preview reaches the catalogue")
    void previewReachesCatalogue() {
        new CatalogListeners(recording()).on(new ShowcasePreviewChanged(SHOWCASE, "https://m/i.jpg", null));

        assertThat(calls).containsExactly(SHOWCASE + " preview https://m/i.jpg null");
    }

    @Test
    @DisplayName("a changed profile reaches every card of its author")
    void profileReachesCatalogue() {
        new CatalogListeners(recording()).on(new ProfileChanged(MEMBER, "CM", List.of("backend", "devops"), "Backend à Douala", "devops"));

        assertThat(calls).containsExactly(MEMBER + " profile CM [backend, devops] Backend à Douala devops");
    }

    @Test
    @DisplayName("what GitHub says about the repository reaches the catalogue")
    void repositoryReachesCatalogue() {
        new CatalogListeners(recording()).on(new ShowcaseRepositoryChanged(SHOWCASE, 7, "A portfolio", List.of("TypeScript"), null));

        assertThat(calls).containsExactly(SHOWCASE + " repository 7 A portfolio [TypeScript]");
    }

    @Test
    @DisplayName("a new like count reaches the catalogue")
    void likesReachCatalogue() {
        new CatalogListeners(recording()).on(new ShowcaseLikesChanged(SHOWCASE, 7));

        assertThat(calls).containsExactly(SHOWCASE + " likes 7");
    }

    @Test
    @DisplayName("a new feedback count reaches the catalogue")
    void feedbackReachesCatalogue() {
        new CatalogListeners(recording()).on(new ShowcaseFeedbackChanged(SHOWCASE, 4));

        assertThat(calls).containsExactly(SHOWCASE + " feedback 4");
    }

    @Test
    @DisplayName("a catalogue failure never reaches the member who published")
    void failureIsContained() {
        CatalogListeners listeners = new CatalogListeners(new CatalogProjection() {
            @Override
            public void add(CatalogEntry entry) {
                throw new IllegalStateException("database down");
            }

            @Override
            public void renameOwner(UUID ownerId, String login) {
                throw new IllegalStateException("database down");
            }

            @Override
            public void updateLikes(UUID showcaseId, int count) {
                throw new IllegalStateException("database down");
            }

            @Override
            public void updateFeedbackCount(UUID showcaseId, int count) {
                throw new IllegalStateException("database down");
            }

            @Override
            public void updatePreview(UUID showcaseId, String imageUrl, String videoUrl) {
                throw new IllegalStateException("database down");
            }

            @Override
            public void updateRepository(UUID showcaseId, Integer stars, String description, List<String> languages, Instant pushedAt) {
                throw new IllegalStateException("database down");
            }

            @Override
            public void updateAuthorProfile(UUID ownerId, String countryCode, List<String> specialties, String headline, String role) {
                throw new IllegalStateException("database down");
            }
        });

        assertThatCode(() -> listeners.on(PUBLISHED)).doesNotThrowAnyException();
        assertThatCode(() -> listeners.on(new MemberSignedIn(MEMBER, "x"))).doesNotThrowAnyException();
        assertThatCode(() -> listeners.on(new ShowcaseLikesChanged(SHOWCASE, 3))).doesNotThrowAnyException();
        assertThatCode(() -> listeners.on(new ShowcaseFeedbackChanged(SHOWCASE, 3))).doesNotThrowAnyException();
    }

    private CatalogProjection recording() {
        return new CatalogProjection() {
            @Override
            public void add(CatalogEntry entry) {
                calls.add(entry);
            }

            @Override
            public void renameOwner(UUID ownerId, String login) {
                calls.add(ownerId + " " + login);
            }

            @Override
            public void updateLikes(UUID showcaseId, int count) {
                calls.add(showcaseId + " likes " + count);
            }

            @Override
            public void updateFeedbackCount(UUID showcaseId, int count) {
                calls.add(showcaseId + " feedback " + count);
            }

            @Override
            public void updatePreview(UUID showcaseId, String imageUrl, String videoUrl) {
                calls.add(showcaseId + " preview " + imageUrl + " " + videoUrl);
            }

            @Override
            public void updateRepository(UUID showcaseId, Integer stars, String description, List<String> languages, Instant pushedAt) {
                calls.add(showcaseId + " repository " + stars + " " + description + " " + languages);
            }

            @Override
            public void updateAuthorProfile(UUID ownerId, String countryCode, List<String> specialties, String headline, String role) {
                calls.add(ownerId + " profile " + countryCode + " " + specialties + " " + headline + " " + role);
            }
        };
    }
}
