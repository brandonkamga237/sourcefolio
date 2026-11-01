package com.brandonkamga.sourcefolio.profile.application;

import java.util.Optional;
import java.util.UUID;

/** What other modules may read of a profile: its public part only. */
public interface ProfileDirectory {

    /** Empty for a member who has answered nothing yet. */
    Optional<PublicProfile> findPublicProfile(UUID memberId);
}
