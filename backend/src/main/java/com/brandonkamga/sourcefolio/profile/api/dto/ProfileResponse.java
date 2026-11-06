package com.brandonkamga.sourcefolio.profile.api.dto;

import com.brandonkamga.sourcefolio.profile.domain.model.DeveloperProfile;
import java.time.Instant;
import java.util.List;

/**
 * country is an ISO code, headline the member's own line and role a slug, null until answered;
 * specialties are slugs, most important first. linkedIn is the handle, linkedInUrl the link
 * built from it. publishedAt is null while the profile is private.
 */
public record ProfileResponse(String country, List<String> specialties, String headline, String role, String city, String bio,
                              String linkedIn, String linkedInUrl, Instant publishedAt) {

    public static ProfileResponse of(DeveloperProfile profile) {
        return new ProfileResponse(profile.country() == null ? null : profile.country().value(), profile.specialties().slugs(),
                profile.headline() == null ? null : profile.headline().value(), profile.role() == null ? null : profile.role().slug(),
                profile.city() == null ? null : profile.city().value(), profile.bio() == null ? null : profile.bio().value(),
                profile.linkedIn() == null ? null : profile.linkedIn().value(),
                profile.linkedIn() == null ? null : profile.linkedIn().url(), profile.publishedAt());
    }
}
