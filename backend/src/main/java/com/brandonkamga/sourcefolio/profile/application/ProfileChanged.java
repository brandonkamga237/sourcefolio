package com.brandonkamga.sourcefolio.profile.application;

import java.util.List;
import java.util.UUID;

/** A member changed their public profile: country code (null until answered) and ranked specialty slugs. */
public record ProfileChanged(UUID memberId, String countryCode, List<String> specialties) {

    public ProfileChanged {
        specialties = List.copyOf(specialties);
    }
}
