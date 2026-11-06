package com.brandonkamga.sourcefolio.profile.application;

import com.brandonkamga.sourcefolio.profile.domain.model.Bio;
import com.brandonkamga.sourcefolio.profile.domain.model.City;
import com.brandonkamga.sourcefolio.profile.domain.model.CountryCode;
import com.brandonkamga.sourcefolio.profile.domain.model.DeveloperProfile;
import com.brandonkamga.sourcefolio.profile.domain.model.Headline;
import com.brandonkamga.sourcefolio.profile.domain.model.LinkedInHandle;
import com.brandonkamga.sourcefolio.profile.domain.model.Role;
import com.brandonkamga.sourcefolio.profile.domain.model.Specialties;
import com.brandonkamga.sourcefolio.profile.domain.model.Specialty;
import com.brandonkamga.sourcefolio.profile.domain.port.ProfileStore;
import com.brandonkamga.sourcefolio.shared.exception.BadRequestException;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class ProfileCompletionImpl implements ProfileCompletion {

    private final ProfileStore profiles;
    private final ProfileEvents events;
    private final Clock clock;

    public ProfileCompletionImpl(ProfileStore profiles, ProfileEvents events, Clock clock) {
        this.profiles = profiles;
        this.events = events;
        this.clock = clock;
    }

    @Override
    public DeveloperProfile findMyProfile(UUID memberId) {
        return profiles.find(memberId).orElseGet(() -> DeveloperProfile.blank(memberId));
    }

    @Override
    public DeveloperProfile answerCountry(UUID memberId, String countryCode) {
        CountryCode country = CountryCode.parse(countryCode).orElseThrow(() -> new BadRequestException("profile.country-invalid"));
        return saveAndAnnounce(findMyProfile(memberId).withCountry(country, clock.instant()));
    }

    @Override
    public DeveloperProfile answerSpecialties(UUID memberId, List<String> specialtySlugs) {
        List<Specialty> ranked = specialtySlugs.stream()
                .map(slug -> Specialty.fromSlug(slug).orElseThrow(() -> new BadRequestException("profile.specialty-invalid")))
                .toList();
        if (ranked.isEmpty() || ranked.size() > Specialties.MAX || ranked.stream().distinct().count() != ranked.size()) {
            throw new BadRequestException("profile.specialties-invalid");
        }
        return saveAndAnnounce(findMyProfile(memberId).withSpecialties(new Specialties(ranked), clock.instant()));
    }

    @Override
    public DeveloperProfile answerHeadline(UUID memberId, String headline) {
        Headline parsed = null;
        if (headline != null && !headline.isBlank()) {
            try {
                parsed = Headline.parse(headline);
            } catch (IllegalArgumentException invalid) {
                throw new BadRequestException("profile.headline-invalid");
            }
        }
        return saveAndAnnounce(findMyProfile(memberId).withHeadline(parsed, clock.instant()));
    }

    @Override
    public DeveloperProfile answerRole(UUID memberId, String roleSlug) {
        Role role = roleSlug == null || roleSlug.isBlank()
                ? null
                : Role.fromSlug(roleSlug.strip()).orElseThrow(() -> new BadRequestException("profile.role-invalid"));
        return saveAndAnnounce(findMyProfile(memberId).withRole(role, clock.instant()));
    }

    @Override
    public DeveloperProfile answerCity(UUID memberId, String city) {
        City parsed = parse(city, City::parse, "profile.city-invalid");
        return saveAndAnnounce(findMyProfile(memberId).withCity(parsed, clock.instant()));
    }

    @Override
    public DeveloperProfile answerBio(UUID memberId, String bio) {
        Bio parsed = parse(bio, Bio::parse, "profile.bio-invalid");
        return saveAndAnnounce(findMyProfile(memberId).withBio(parsed, clock.instant()));
    }

    @Override
    public DeveloperProfile answerLinkedIn(UUID memberId, String linkedIn) {
        LinkedInHandle parsed = linkedIn == null || linkedIn.isBlank()
                ? null
                : LinkedInHandle.parse(linkedIn).orElseThrow(() -> new BadRequestException("profile.linkedin-invalid"));
        return saveAndAnnounce(findMyProfile(memberId).withLinkedIn(parsed, clock.instant()));
    }

    @Override
    public DeveloperProfile publish(UUID memberId) {
        DeveloperProfile profile = findMyProfile(memberId);
        if (profile.country() == null) {
            throw new BadRequestException("profile.country-required");
        }
        return profile.published() ? profile : saveAndAnnounce(profile.publish(clock.instant()));
    }

    @Override
    public DeveloperProfile unpublish(UUID memberId) {
        DeveloperProfile profile = findMyProfile(memberId);
        return profile.published() ? saveAndAnnounce(profile.unpublish(clock.instant())) : profile;
    }

    private static <T> T parse(String raw, Function<String, T> parser, String error) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return parser.apply(raw);
        } catch (IllegalArgumentException invalid) {
            throw new BadRequestException(error);
        }
    }

    // Cards and member popups show the profile, Discover filters on the country and the role:
    // the catalogue keeps a copy.
    private DeveloperProfile saveAndAnnounce(DeveloperProfile profile) {
        DeveloperProfile saved = profiles.save(profile);
        events.profileChanged(new ProfileChanged(saved.memberId(), saved.country() == null ? null : saved.country().value(),
                saved.specialties().slugs(), saved.headline() == null ? null : saved.headline().value(),
                saved.role() == null ? null : saved.role().slug()));
        return saved;
    }
}
