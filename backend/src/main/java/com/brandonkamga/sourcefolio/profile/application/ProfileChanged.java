package com.brandonkamga.sourcefolio.profile.application;

import java.util.List;
import java.util.UUID;

/**
 * A member changed their public profile: country code and headline (null until answered) and
 * ranked interest slugs.
 */
public record ProfileChanged(UUID memberId, String countryCode, List<String> specialties, String headline) {

    public ProfileChanged {
        specialties = List.copyOf(specialties);
    }
}
