package com.brandonkamga.sourcefolio.profile.application;

import java.util.List;
import java.util.UUID;

/**
 * A member changed their profile: country code, headline and role slug (null until answered),
 * ranked interest slugs, the city they gave, and whether they consented to a public profile.
 */
public record ProfileChanged(UUID memberId, String countryCode, List<String> specialties, String headline, String role,
                             String city, boolean published) {

    public ProfileChanged(UUID memberId, String countryCode, List<String> specialties, String headline, String role) {
        this(memberId, countryCode, specialties, headline, role, null, false);
    }

    public ProfileChanged {
        specialties = List.copyOf(specialties);
    }
}
