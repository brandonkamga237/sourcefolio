package com.brandonkamga.sourcefolio.profile.application;

import com.brandonkamga.sourcefolio.profile.domain.port.ProfileStore;
import java.util.Optional;
import java.util.UUID;

public class ProfileDirectoryImpl implements ProfileDirectory {

    private final ProfileStore profiles;

    public ProfileDirectoryImpl(ProfileStore profiles) {
        this.profiles = profiles;
    }

    @Override
    public Optional<PublicProfile> findPublicProfile(UUID memberId) {
        return profiles.find(memberId).map(profile ->
                new PublicProfile(profile.country() == null ? null : profile.country().value(), profile.specialties().slugs(),
                        profile.headline() == null ? null : profile.headline().value(), profile.role() == null ? null : profile.role().slug()));
    }
}
