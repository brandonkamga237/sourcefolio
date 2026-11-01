package com.brandonkamga.sourcefolio.profile.application;

import com.brandonkamga.sourcefolio.profile.domain.model.CountryCode;
import com.brandonkamga.sourcefolio.profile.domain.model.DeveloperProfile;
import com.brandonkamga.sourcefolio.profile.domain.model.Specialties;
import com.brandonkamga.sourcefolio.profile.domain.model.Specialty;
import com.brandonkamga.sourcefolio.profile.domain.port.ProfileStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class ProfileDirectoryImplTest {

    private static final UUID MEMBER = UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11");

    @Test
    @DisplayName("the public part of a profile is its country and its ranked specialties; nothing before the first answer")
    void publicProfile() {
        DeveloperProfile stored = new DeveloperProfile(MEMBER, new CountryCode("SN"), new Specialties(List.of(Specialty.MOBILE, Specialty.FRONTEND)),
                Instant.parse("2026-10-30T09:00:00Z"));
        ProfileDirectory directory = new ProfileDirectoryImpl(new ProfileStore() {
            @Override
            public Optional<DeveloperProfile> find(UUID memberId) {
                return memberId.equals(MEMBER) ? Optional.of(stored) : Optional.empty();
            }

            @Override
            public DeveloperProfile save(DeveloperProfile profile) {
                throw new AssertionError("reading saves nothing");
            }
        });

        assertThat(directory.findPublicProfile(MEMBER)).contains(new PublicProfile("SN", List.of("mobile", "frontend")));
        assertThat(directory.findPublicProfile(UUID.randomUUID())).isEmpty();
    }
}
