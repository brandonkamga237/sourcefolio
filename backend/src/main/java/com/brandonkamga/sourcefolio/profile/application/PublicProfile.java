package com.brandonkamga.sourcefolio.profile.application;

import java.util.List;

/** What anyone may see of a member's profile: country code (null until answered) and ranked specialty slugs. */
public record PublicProfile(String countryCode, List<String> specialties) {

    public PublicProfile {
        specialties = List.copyOf(specialties);
    }
}
