package com.brandonkamga.sourcefolio.profile.infra.persistence;

import com.brandonkamga.sourcefolio.profile.domain.model.CountryCode;
import com.brandonkamga.sourcefolio.profile.domain.model.DeveloperProfile;
import com.brandonkamga.sourcefolio.profile.domain.model.Specialty;
import com.brandonkamga.sourcefolio.profile.domain.port.ProfileStore;
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
                profile.specialty() == null ? null : profile.specialty().slug(), profile.updatedAt());
        return toDomain(profiles.save(row));
    }

    private static DeveloperProfile toDomain(DeveloperProfileEntity row) {
        return new DeveloperProfile(row.getMemberId(),
                row.getCountryCode() == null ? null : new CountryCode(row.getCountryCode()),
                row.getSpecialty() == null ? null : Specialty.fromSlug(row.getSpecialty()).orElseThrow(),
                row.getUpdatedAt());
    }
}
