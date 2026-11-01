package com.brandonkamga.sourcefolio.profile.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * What a member declares about themselves. The country and headline stay null and the interests
 * empty until answered.
 */
public record DeveloperProfile(UUID memberId, CountryCode country, Specialties specialties, Headline headline, Instant updatedAt) {

    /** A profile without a headline. */
    public DeveloperProfile(UUID memberId, CountryCode country, Specialties specialties, Instant updatedAt) {
        this(memberId, country, specialties, null, updatedAt);
    }

    public DeveloperProfile {
        Objects.requireNonNull(memberId, "memberId");
        specialties = specialties == null ? Specialties.none() : specialties;
    }

    /** A member who has answered nothing yet. */
    public static DeveloperProfile blank(UUID memberId) {
        return new DeveloperProfile(memberId, null, Specialties.none(), null, null);
    }

    public DeveloperProfile withCountry(CountryCode country, Instant now) {
        return new DeveloperProfile(memberId, Objects.requireNonNull(country, "country"), specialties, headline, now);
    }

    public DeveloperProfile withSpecialties(Specialties specialties, Instant now) {
        return new DeveloperProfile(memberId, country, Objects.requireNonNull(specialties, "specialties"), headline, now);
    }

    /** Sets the member's own line, or removes it with null. */
    public DeveloperProfile withHeadline(Headline headline, Instant now) {
        return new DeveloperProfile(memberId, country, specialties, headline, now);
    }
}
