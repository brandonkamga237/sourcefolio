package com.brandonkamga.sourcefolio.discovery.application;

import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogEntry;
import com.brandonkamga.sourcefolio.discovery.domain.port.CatalogStore;
import java.util.UUID;

public class CatalogProjectionImpl implements CatalogProjection {

    private final CatalogStore store;

    public CatalogProjectionImpl(CatalogStore store) {
        this.store = store;
    }

    @Override
    public void add(CatalogEntry entry) {
        store.upsert(entry);
    }

    @Override
    public void renameOwner(UUID ownerId, String login) {
        store.renameOwner(ownerId, login);
    }

    @Override
    public void updateLikes(UUID showcaseId, int count) {
        store.setLikes(showcaseId, count);
    }

    @Override
    public void updatePreview(UUID showcaseId, String imageUrl, String videoUrl) {
        store.setPreview(showcaseId, imageUrl, videoUrl);
    }

    @Override
    public void updateRepositoryStars(UUID showcaseId, Integer stars) {
        store.setRepositoryStars(showcaseId, stars);
    }
}
