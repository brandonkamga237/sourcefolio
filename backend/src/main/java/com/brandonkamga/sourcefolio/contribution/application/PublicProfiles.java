package com.brandonkamga.sourcefolio.contribution.application;

import java.util.Optional;

public interface PublicProfiles {

    /**
     * Empty unless the member published their profile and has a snapshot: nothing is public
     * without consent, and a profile without a reading has nothing to prove.
     */
    Optional<ProfileCard> findByLogin(String login);
}
