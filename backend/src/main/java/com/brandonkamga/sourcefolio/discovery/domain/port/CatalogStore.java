package com.brandonkamga.sourcefolio.discovery.domain.port;

import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogEntry;
import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogFacets;
import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogFilter;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankedEntry;
import com.brandonkamga.sourcefolio.discovery.domain.model.RecentCursor;
import com.brandonkamga.sourcefolio.discovery.domain.model.Shuffled;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CatalogStore {

    /** Adds the entry, or replaces the one of the same showcase: an event delivered twice changes nothing. */
    void upsert(CatalogEntry entry);

    /** Follows a GitHub rename on every card of the member. */
    void renameOwner(UUID ownerId, String login);

    /** Keeps the copy of the preview the author chose; null URLs mean none. */
    void setPreview(UUID showcaseId, String imageUrl, String videoUrl);

    /** Keeps the copy of a showcase's like count, announced by the engagement module. */
    void setLikes(UUID showcaseId, int count);

    /** Keeps the copy of the repository's GitHub stars; null when unknown. */
    void setRepositoryStars(UUID showcaseId, Integer stars);

    /** Keeps the copy of an author's public profile on every card of theirs; null country or headline when unanswered. */
    void setAuthorProfile(UUID ownerId, String countryCode, List<String> specialties, String headline);

    /** The most liked showcases first, ties broken by the most recent; liked ones only. */
    List<RankedEntry> ranking(int limit);

    /**
     * The day's shuffle of the entries published since {@code youngSince} (young) or before it
     * (not young), after {@code afterKey} (empty for the start), at most {@code limit}.
     */
    List<Shuffled> shuffled(LocalDate day, boolean young, Instant youngSince, String afterKey, int limit, CatalogFilter filter);

    /** The cards of the member with this GitHub login, any case, newest first. */
    List<CatalogEntry> byOwnerLogin(String login);

    /** The countries and interests the catalogue's cards carry, with their counts, most frequent first. */
    CatalogFacets facets();

    /** How many showcases the catalogue lists under these filters. */
    long count(CatalogFilter filter);

    /** Newest first, after {@code after} (null for the start), at most {@code limit}. */
    List<CatalogEntry> recent(RecentCursor after, int limit, CatalogFilter filter);
}
