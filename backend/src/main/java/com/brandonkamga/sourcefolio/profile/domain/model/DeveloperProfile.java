package com.brandonkamga.sourcefolio.profile.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** What a member declares about themselves. Each field stays null until answered. */
public record DeveloperProfile(UUID memberId, CountryCode country, Specialty specialty, Instant updatedAt) {

    public DeveloperProfile {
        Objects.requireNonNull(memberId, "memberId");
    }

    /** A member who has answered nothing yet. */
    public static DeveloperProfile blank(UUID memberId) {
        return new DeveloperProfile(memberId, null, null, null);
    }

    public DeveloperProfile withCountry(CountryCode country, Instant now) {
        return new DeveloperProfile(memberId, Objects.requireNonNull(country, "country"), specialty, now);
    }

    public DeveloperProfile withSpecialty(Specialty specialty, Instant now) {
        return new DeveloperProfile(memberId, country, Objects.requireNonNull(specialty, "specialty"), now);
    }
}
