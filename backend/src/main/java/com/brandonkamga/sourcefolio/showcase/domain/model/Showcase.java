package com.brandonkamga.sourcefolio.showcase.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * What a member publishes, with the repository it is built from. Status changes are recorded as
 * {@link ShowcaseStatusEvent}s waiting in {@link #pendingEvents()} until the store appends them
 * to the history; a stored copy has none.
 */
public record Showcase(ShowcaseId id, ShowcaseSlug slug, UUID ownerId, ShowcaseKind kind, SiteUrl siteUrl, SourceRepo sourceRepo,
                       ShowcaseStatus status, Instant createdAt, Instant publishedAt,
                       List<ShowcaseStatusEvent> pendingEvents) {

    public Showcase {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(slug, "slug");
        Objects.requireNonNull(ownerId, "ownerId");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(siteUrl, "siteUrl");
        Objects.requireNonNull(sourceRepo, "sourceRepo");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(createdAt, "createdAt");
        pendingEvents = List.copyOf(pendingEvents);
    }

    /**
     * Publishes a detected candidate whose checks passed (the spec's automatic publication: the
     * repository belongs to the member, its homepage or their GitHub Pages is the site, and the
     * site answers). The caller has made those checks.
     */
    public static Showcase publishVerified(UUID ownerId, ShowcaseSlug slug, ShowcaseCandidate candidate, Instant now) {
        return new Showcase(ShowcaseId.newId(), slug, ownerId, ShowcaseKind.PORTFOLIO, candidate.siteUrl(),
                new SourceRepo(RepositoryProvider.GITHUB, candidate.repository(), true),
                ShowcaseStatus.PUBLISHED, now, now,
                List.of(new ShowcaseStatusEvent(null, ShowcaseStatus.PUBLISHED, ShowcaseStatusEvent.AUTO_VERIFIED,
                        StatusActor.SYSTEM, now)));
    }

    /** The same showcase once its pending events are in the history. */
    public Showcase stored() {
        return new Showcase(id, slug, ownerId, kind, siteUrl, sourceRepo, status, createdAt, publishedAt, List.of());
    }
}
