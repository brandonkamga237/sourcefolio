package com.brandonkamga.sourcefolio.profile.api.dto;

import com.brandonkamga.sourcefolio.profile.domain.model.DeveloperProfile;

/** country is an ISO code, specialty a slug; each is null until answered. */
public record ProfileResponse(String country, String specialty) {

    public static ProfileResponse of(DeveloperProfile profile) {
        return new ProfileResponse(profile.country() == null ? null : profile.country().value(),
                profile.specialty() == null ? null : profile.specialty().slug());
    }
}
