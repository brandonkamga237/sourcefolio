package com.brandonkamga.sourcefolio.profile.application;

import java.util.List;
import java.util.Set;

/**
 * What anyone may see of a member's profile: country code, headline and role slug (null until
 * answered) and ranked interest slugs; city, bio and LinkedIn as the member wrote them.
 * published says whether the member consented to a public profile page.
 */
public record PublicProfile(String countryCode, List<String> specialties, String headline, String role, String city, String bio,
                            String linkedInUrl, boolean published) {

    private static final Set<String> SECTORS = Set.of("fintech", "healthtech", "edtech", "ecommerce", "agritech", "iot", "robotics", "ar-vr");

    public PublicProfile(String countryCode, List<String> specialties, String headline, String role) {
        this(countryCode, specialties, headline, role, null, null, null, false);
    }

    public PublicProfile {
        specialties = List.copyOf(specialties);
    }

    /** The sector the member declares: shown on their profile, never a filter or a ranking. */
    public String sector() {
        return specialties.stream().filter(SECTORS::contains).findFirst().orElse(null);
    }
}
