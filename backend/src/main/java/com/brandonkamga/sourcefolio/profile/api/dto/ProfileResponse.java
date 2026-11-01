package com.brandonkamga.sourcefolio.profile.api.dto;

import com.brandonkamga.sourcefolio.profile.domain.model.DeveloperProfile;
import java.util.List;

/** country is an ISO code and headline the member's own line, null until answered; specialties are slugs, most important first. */
public record ProfileResponse(String country, List<String> specialties, String headline) {

    public static ProfileResponse of(DeveloperProfile profile) {
        return new ProfileResponse(profile.country() == null ? null : profile.country().value(), profile.specialties().slugs(),
                profile.headline() == null ? null : profile.headline().value());
    }
}
