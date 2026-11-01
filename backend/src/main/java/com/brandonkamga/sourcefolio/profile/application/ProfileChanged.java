package com.brandonkamga.sourcefolio.profile.application;

import java.util.List;
import java.util.UUID;

/**
 * A member changed their public profile: country code, headline and role slug (null until
 * answered) and ranked interest slugs.
 */
public record ProfileChanged(UUID memberId, String countryCode, List<String> specialties, String headline, String role) {

    public ProfileChanged {
        specialties = List.copyOf(specialties);
    }
}
