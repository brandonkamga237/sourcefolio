package com.brandonkamga.sourcefolio.profile.domain.port;

import com.brandonkamga.sourcefolio.profile.domain.model.DeveloperProfile;
import java.util.Optional;
import java.util.UUID;

public interface ProfileStore {

    Optional<DeveloperProfile> find(UUID memberId);

    /** Creates the profile on the first answer, updates it afterwards. Returns the stored copy. */
    DeveloperProfile save(DeveloperProfile profile);
}
