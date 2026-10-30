package com.brandonkamga.sourcefolio.profile.application;

import com.brandonkamga.sourcefolio.profile.domain.model.CountryCode;
import com.brandonkamga.sourcefolio.profile.domain.model.DeveloperProfile;
import com.brandonkamga.sourcefolio.profile.domain.model.Specialty;
import com.brandonkamga.sourcefolio.profile.domain.port.ProfileStore;
import com.brandonkamga.sourcefolio.shared.exception.BadRequestException;
import java.time.Clock;
import java.util.UUID;

public class ProfileCompletionImpl implements ProfileCompletion {

    private final ProfileStore profiles;
    private final Clock clock;

    public ProfileCompletionImpl(ProfileStore profiles, Clock clock) {
        this.profiles = profiles;
        this.clock = clock;
    }

    @Override
    public DeveloperProfile findMyProfile(UUID memberId) {
        return profiles.find(memberId).orElseGet(() -> DeveloperProfile.blank(memberId));
    }

    @Override
    public DeveloperProfile answerCountry(UUID memberId, String countryCode) {
        CountryCode country = CountryCode.parse(countryCode).orElseThrow(() -> new BadRequestException("profile.country-invalid"));
        return profiles.save(findMyProfile(memberId).withCountry(country, clock.instant()));
    }

    @Override
    public DeveloperProfile answerSpecialty(UUID memberId, String specialtySlug) {
        Specialty specialty = Specialty.fromSlug(specialtySlug).orElseThrow(() -> new BadRequestException("profile.specialty-invalid"));
        return profiles.save(findMyProfile(memberId).withSpecialty(specialty, clock.instant()));
    }
}
