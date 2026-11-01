package com.brandonkamga.sourcefolio.profile.application;

import java.util.List;

/**
 * What anyone may see of a member's profile: country code, headline and role slug (null until
 * answered) and ranked interest slugs.
 */
public record PublicProfile(String countryCode, List<String> specialties, String headline, String role) {

    public PublicProfile {
        specialties = List.copyOf(specialties);
    }
}
