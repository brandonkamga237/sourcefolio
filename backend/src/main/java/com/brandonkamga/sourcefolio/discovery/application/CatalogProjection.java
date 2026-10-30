package com.brandonkamga.sourcefolio.discovery.application;

import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogEntry;
import java.util.UUID;

/** Keeps the catalogue in step with what other modules announce. Safe to call twice. */
public interface CatalogProjection {

    void add(CatalogEntry entry);

    void renameOwner(UUID ownerId, String login);

    void updateLikes(UUID showcaseId, int count);
}
