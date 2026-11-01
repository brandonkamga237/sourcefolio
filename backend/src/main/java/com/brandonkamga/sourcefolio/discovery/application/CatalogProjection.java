package com.brandonkamga.sourcefolio.discovery.application;

import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogEntry;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Keeps the catalogue in step with what other modules announce. Safe to call twice. */
public interface CatalogProjection {

    void add(CatalogEntry entry);

    void renameOwner(UUID ownerId, String login);

    void updateLikes(UUID showcaseId, int count);

    void updateFeedbackCount(UUID showcaseId, int count);

    void updatePreview(UUID showcaseId, String imageUrl, String videoUrl);

    void updateRepository(UUID showcaseId, Integer stars, String description, List<String> languages, Instant pushedAt);

    void updateAuthorProfile(UUID ownerId, String countryCode, List<String> specialties, String headline, String role);
}
