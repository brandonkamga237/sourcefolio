package com.brandonkamga.sourcefolio.profile.application;

import com.brandonkamga.sourcefolio.profile.domain.model.CountryCode;
import com.brandonkamga.sourcefolio.profile.domain.model.DeveloperProfile;
import com.brandonkamga.sourcefolio.profile.domain.model.Specialty;
import com.brandonkamga.sourcefolio.profile.domain.port.ProfileStore;
import com.brandonkamga.sourcefolio.shared.exception.AppException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfileCompletionImplTest {

    private static final Instant NOW = Instant.parse("2026-10-30T09:00:00Z");
    private static final UUID MEMBER = UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11");

    private final Map<UUID, DeveloperProfile> rows = new HashMap<>();
    private final ProfileStore store = new ProfileStore() {
        @Override
        public Optional<DeveloperProfile> find(UUID memberId) {
            return Optional.ofNullable(rows.get(memberId));
        }

        @Override
        public DeveloperProfile save(DeveloperProfile profile) {
            rows.put(profile.memberId(), profile);
            return profile;
        }
    };
    private final ProfileCompletion profiles = new ProfileCompletionImpl(store, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    @DisplayName("before any answer the profile is blank, and nothing is stored")
    void blankFirst() {
        assertThat(profiles.findMyProfile(MEMBER)).isEqualTo(DeveloperProfile.blank(MEMBER));
        assertThat(rows).isEmpty();
    }

    @Test
    @DisplayName("each answer is saved on its own and keeps the earlier ones")
    void answersAccumulate() {
        profiles.answerCountry(MEMBER, "cm");
        profiles.answerSpecialty(MEMBER, "fullstack");

        assertThat(rows.get(MEMBER)).isEqualTo(new DeveloperProfile(MEMBER, new CountryCode("CM"), Specialty.FULLSTACK, NOW));
    }

    @Test
    @DisplayName("an answer outside the lists is refused and changes nothing")
    void invalidAnswers() {
        profiles.answerCountry(MEMBER, "CM");

        assertThatThrownBy(() -> profiles.answerCountry(MEMBER, "Cameroun"))
                .isInstanceOf(AppException.class)
                .extracting(thrown -> ((AppException) thrown).messageKey()).isEqualTo("profile.country-invalid");
        assertThatThrownBy(() -> profiles.answerSpecialty(MEMBER, "wizard"))
                .isInstanceOf(AppException.class)
                .extracting(thrown -> ((AppException) thrown).messageKey()).isEqualTo("profile.specialty-invalid");
        assertThat(rows.get(MEMBER).country()).isEqualTo(new CountryCode("CM"));
        assertThat(rows.get(MEMBER).specialty()).isNull();
    }
}
