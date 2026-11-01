package com.brandonkamga.sourcefolio.profile.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** What a member declares about themselves. The country stays null and the specialties empty until answered. */
public record DeveloperProfile(UUID memberId, CountryCode country, Specialties specialties, Instant updatedAt) {

    public DeveloperProfile {
        Objects.requireNonNull(memberId, "memberId");
        specialties = specialties == null ? Specialties.none() : specialties;
    }

    /** A member who has answered nothing yet. */
    public static DeveloperProfile blank(UUID memberId) {
        return new DeveloperProfile(memberId, null, Specialties.none(), null);
    }

    public DeveloperProfile withCountry(CountryCode country, Instant now) {
        return new DeveloperProfile(memberId, Objects.requireNonNull(country, "country"), specialties, now);
    }

    public DeveloperProfile withSpecialties(Specialties specialties, Instant now) {
        return new DeveloperProfile(memberId, country, Objects.requireNonNull(specialties, "specialties"), now);
    }
}
