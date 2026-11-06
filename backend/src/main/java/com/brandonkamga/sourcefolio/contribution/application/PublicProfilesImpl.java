package com.brandonkamga.sourcefolio.contribution.application;

import com.brandonkamga.sourcefolio.contribution.domain.port.ContributionStore;
import com.brandonkamga.sourcefolio.profile.application.ProfileDirectory;
import com.brandonkamga.sourcefolio.profile.application.PublicProfile;
import java.util.Optional;

public class PublicProfilesImpl implements PublicProfiles {

    private final ContributionStore records;
    private final ProfileDirectory profiles;

    public PublicProfilesImpl(ContributionStore records, ProfileDirectory profiles) {
        this.records = records;
        this.profiles = profiles;
    }

    @Override
    public Optional<ProfileCard> findByLogin(String login) {
        return records.findByLogin(login)
                .filter(record -> record.snapshot() != null)
                .flatMap(record -> profiles.findPublicProfile(record.memberId())
                        .filter(PublicProfile::published)
                        .map(profile -> new ProfileCard(record.login(), record.snapshot(), profile)));
    }
}
