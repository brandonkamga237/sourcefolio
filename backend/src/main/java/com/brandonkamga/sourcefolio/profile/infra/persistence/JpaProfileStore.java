package com.brandonkamga.sourcefolio.profile.infra.persistence;

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
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class JpaProfileStore implements ProfileStore {

    private final DeveloperProfileJpaRepository profiles;

    JpaProfileStore(DeveloperProfileJpaRepository profiles) {
        this.profiles = profiles;
    }

    @Override
    public Optional<DeveloperProfile> find(UUID memberId) {
        return profiles.findById(memberId).map(JpaProfileStore::toDomain);
    }

    @Override
    public DeveloperProfile save(DeveloperProfile profile) {
        DeveloperProfileEntity row = profiles.findById(profile.memberId())
                .orElseGet(() -> new DeveloperProfileEntity(profile.memberId()));
        row.update(profile.country() == null ? null : profile.country().value(),
                profile.specialties().slugs().toArray(String[]::new), profile.headline() == null ? null : profile.headline().value(),
                profile.role() == null ? null : profile.role().slug(), profile.updatedAt());
        row.present(profile.city() == null ? null : profile.city().value(), profile.bio() == null ? null : profile.bio().value(),
                profile.linkedIn() == null ? null : profile.linkedIn().value(), profile.publishedAt());
        return toDomain(profiles.save(row));
    }

    private static DeveloperProfile toDomain(DeveloperProfileEntity row) {
        return new DeveloperProfile(row.getMemberId(),
                row.getCountryCode() == null ? null : new CountryCode(row.getCountryCode()),
                new Specialties(Arrays.stream(row.getSpecialties()).map(slug -> Specialty.fromSlug(slug).orElseThrow()).toList()),
                row.getHeadline() == null ? null : new Headline(row.getHeadline()),
                row.getRole() == null ? null : Role.fromSlug(row.getRole()).orElseThrow(),
                row.getCity() == null ? null : new City(row.getCity()),
                row.getBio() == null ? null : new Bio(row.getBio()),
                row.getLinkedin() == null ? null : new LinkedInHandle(row.getLinkedin()),
                row.getPublishedAt(), row.getUpdatedAt());
    }
}
