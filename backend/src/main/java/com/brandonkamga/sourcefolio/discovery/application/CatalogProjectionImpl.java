package com.brandonkamga.sourcefolio.discovery.application;

import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogEntry;
import com.brandonkamga.sourcefolio.discovery.domain.port.CatalogStore;
import com.brandonkamga.sourcefolio.profile.application.ProfileDirectory;
import java.util.List;
import java.util.UUID;

public class CatalogProjectionImpl implements CatalogProjection {

    private final CatalogStore store;
    private final ProfileDirectory profiles;

    public CatalogProjectionImpl(CatalogStore store, ProfileDirectory profiles) {
        this.store = store;
        this.profiles = profiles;
    }

    // A member usually answers their profile before publishing: the new card starts with it.
    @Override
    public void add(CatalogEntry entry) {
        store.upsert(entry);
        profiles.findPublicProfile(entry.ownerId())
                .ifPresent(profile -> store.setAuthorProfile(entry.ownerId(), profile.countryCode(), profile.specialties(), profile.headline()));
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

    @Override
    public void updateAuthorProfile(UUID ownerId, String countryCode, List<String> specialties, String headline) {
        store.setAuthorProfile(ownerId, countryCode, specialties, headline);
    }
}
