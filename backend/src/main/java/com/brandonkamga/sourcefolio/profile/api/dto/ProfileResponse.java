package com.brandonkamga.sourcefolio.profile.api.dto;

import com.brandonkamga.sourcefolio.profile.domain.model.DeveloperProfile;
import java.util.List;

/**
 * country is an ISO code, headline the member's own line and role a slug, null until answered;
 * specialties are slugs, most important first.
 */
public record ProfileResponse(String country, List<String> specialties, String headline, String role) {

    public static ProfileResponse of(DeveloperProfile profile) {
        return new ProfileResponse(profile.country() == null ? null : profile.country().value(), profile.specialties().slugs(),
                profile.headline() == null ? null : profile.headline().value(), profile.role() == null ? null : profile.role().slug());
    }
}
