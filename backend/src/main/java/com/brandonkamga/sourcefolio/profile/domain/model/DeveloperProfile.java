package com.brandonkamga.sourcefolio.profile.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * What a member declares about themselves. Every field stays null (the interests empty) until
 * answered. A profile is private until publishedAt is set: nothing is public without the
 * member's explicit consent, and it takes a country, since rankings are per country.
 */
public record DeveloperProfile(UUID memberId, CountryCode country, Specialties specialties, Headline headline, Role role,
                               City city, Bio bio, LinkedInHandle linkedIn, Instant publishedAt, Instant updatedAt) {

    /** A profile without a presentation and never published. */
    public DeveloperProfile(UUID memberId, CountryCode country, Specialties specialties, Headline headline, Role role,
                            Instant updatedAt) {
        this(memberId, country, specialties, headline, role, null, null, null, null, updatedAt);
    }

    /** A profile without a headline or a role. */
    public DeveloperProfile(UUID memberId, CountryCode country, Specialties specialties, Instant updatedAt) {
        this(memberId, country, specialties, null, null, updatedAt);
    }

    public DeveloperProfile {
        Objects.requireNonNull(memberId, "memberId");
        specialties = specialties == null ? Specialties.none() : specialties;
    }

    /** A member who has answered nothing yet. */
    public static DeveloperProfile blank(UUID memberId) {
        return new DeveloperProfile(memberId, null, Specialties.none(), null, null, null, null, null, null, null);
    }

    public DeveloperProfile withCountry(CountryCode country, Instant now) {
        return new DeveloperProfile(memberId, Objects.requireNonNull(country, "country"), specialties, headline, role, city, bio, linkedIn, publishedAt, now);
    }

    public DeveloperProfile withSpecialties(Specialties specialties, Instant now) {
        return new DeveloperProfile(memberId, country, Objects.requireNonNull(specialties, "specialties"), headline, role, city, bio, linkedIn, publishedAt, now);
    }

    /** Sets the member's own line, or removes it with null. */
    public DeveloperProfile withHeadline(Headline headline, Instant now) {
        return new DeveloperProfile(memberId, country, specialties, headline, role, city, bio, linkedIn, publishedAt, now);
    }

    /** Sets the role the member says they work in, or removes it with null. */
    public DeveloperProfile withRole(Role role, Instant now) {
        return new DeveloperProfile(memberId, country, specialties, headline, role, city, bio, linkedIn, publishedAt, now);
    }

    /** Sets the city shown on the profile and used by the map, or removes it with null. */
    public DeveloperProfile withCity(City city, Instant now) {
        return new DeveloperProfile(memberId, country, specialties, headline, role, city, bio, linkedIn, publishedAt, now);
    }

    public DeveloperProfile withBio(Bio bio, Instant now) {
        return new DeveloperProfile(memberId, country, specialties, headline, role, city, bio, linkedIn, publishedAt, now);
    }

    public DeveloperProfile withLinkedIn(LinkedInHandle linkedIn, Instant now) {
        return new DeveloperProfile(memberId, country, specialties, headline, role, city, bio, linkedIn, publishedAt, now);
    }

    public boolean published() {
        return publishedAt != null;
    }

    /** Publishing twice keeps the first date. */
    public DeveloperProfile publish(Instant now) {
        if (country == null) {
            throw new IllegalStateException("A profile needs a country to be published");
        }
        return published() ? this : new DeveloperProfile(memberId, country, specialties, headline, role, city, bio, linkedIn, now, now);
    }

    public DeveloperProfile unpublish(Instant now) {
        return published() ? new DeveloperProfile(memberId, country, specialties, headline, role, city, bio, linkedIn, null, now) : this;
    }
}
