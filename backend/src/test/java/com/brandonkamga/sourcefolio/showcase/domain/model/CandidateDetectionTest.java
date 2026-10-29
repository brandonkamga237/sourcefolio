package com.brandonkamga.sourcefolio.showcase.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class CandidateDetectionTest {

    private static final Instant OLD = Instant.parse("2025-01-01T00:00:00Z");
    private static final Instant RECENT = Instant.parse("2026-10-01T00:00:00Z");

    @Test
    @DisplayName("the {login}.github.io repository comes first, at the member's github.io address")
    void userPagesFirst() {
        List<ShowcaseCandidate> found = CandidateDetection.detect("NEkwalla", List.of(
                repo("api", "https://api.example.org", false, RECENT),
                repo("nekwalla.github.io", null, true, OLD)));

        assertThat(found).extracting(c -> c.repository().name(), c -> c.siteUrl().value(), ShowcaseCandidate::reason)
                .containsExactly(
                        tuple("nekwalla.github.io", "https://nekwalla.github.io/", CandidateReason.USER_PAGES),
                        tuple("api", "https://api.example.org/", CandidateReason.HOMEPAGE));
    }

    @Test
    @DisplayName("a github.io repository with a custom domain points to that domain")
    void userPagesCustomDomain() {
        assertThat(CandidateDetection.detect("nekwalla", List.of(repo("nekwalla.github.io", "nekwalla.dev", true, OLD))))
                .extracting(c -> c.siteUrl().value()).containsExactly("https://nekwalla.dev/");
    }

    @Test
    @DisplayName("a repository named portfolio, site or me outranks a product site found through a homepage")
    void namedRepositoryBeforeHomepage() {
        List<ShowcaseCandidate> found = CandidateDetection.detect("nekwalla", List.of(
                repo("ghemit-platform", "https://gehmit.org/", false, RECENT),
                repo("Portfolio", "https://nekwalla.dev", false, OLD)));

        assertThat(found).extracting(ShowcaseCandidate::reason)
                .containsExactly(CandidateReason.NAMED_REPOSITORY, CandidateReason.HOMEPAGE);
    }

    @Test
    @DisplayName("a named repository published with GitHub Pages and no homepage gets its Pages address")
    void namedRepositoryOnPages() {
        assertThat(CandidateDetection.detect("nekwalla", List.of(repo("me", null, true, OLD))))
                .extracting(c -> c.siteUrl().value(), ShowcaseCandidate::reason)
                .containsExactly(tuple("https://nekwalla.github.io/me/", CandidateReason.NAMED_REPOSITORY));
    }

    @Test
    @DisplayName("a repository without any site is not a candidate, whatever its name")
    void noSiteNoCandidate() {
        assertThat(CandidateDetection.detect("nekwalla", List.of(
                repo("portfolio", null, false, RECENT),
                repo("notes", "TBD", false, RECENT)))).isEmpty();
    }

    @Test
    @DisplayName("any other repository on GitHub Pages comes last")
    void projectPagesLast() {
        assertThat(CandidateDetection.detect("nekwalla", List.of(
                repo("talk-slides", null, true, RECENT),
                repo("shop", "shop.example.org", false, OLD))))
                .extracting(ShowcaseCandidate::reason)
                .containsExactly(CandidateReason.HOMEPAGE, CandidateReason.PROJECT_PAGES);
    }

    @Test
    @DisplayName("a site reached from two repositories appears once, under its strongest reason")
    void oneCandidatePerSite() {
        List<ShowcaseCandidate> found = CandidateDetection.detect("nekwalla", List.of(
                repo("blog", "https://nekwalla.dev", false, RECENT),
                repo("site", "https://nekwalla.dev/", false, OLD)));

        assertThat(found).singleElement().satisfies(candidate -> {
            assertThat(candidate.repository().name()).isEqualTo("site");
            assertThat(candidate.reason()).isEqualTo(CandidateReason.NAMED_REPOSITORY);
        });
    }

    @Test
    @DisplayName("within the same reason, the most recently pushed repository comes first")
    void recentFirstWithinReason() {
        assertThat(CandidateDetection.detect("nekwalla", List.of(
                repo("old-shop", "old.example.org", false, OLD),
                repo("new-shop", "new.example.org", false, RECENT),
                repo("never-pushed", "empty.example.org", false, null))))
                .extracting(c -> c.repository().name())
                .containsExactly("new-shop", "old-shop", "never-pushed");
    }

    private static PublicRepository repo(String name, String homepage, boolean pages, Instant pushedAt) {
        return new PublicRepository(new RepositoryCoordinates("nekwalla", name), homepage, pages, pushedAt);
    }
}
