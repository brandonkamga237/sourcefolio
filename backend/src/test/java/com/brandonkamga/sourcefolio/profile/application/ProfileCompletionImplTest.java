package com.brandonkamga.sourcefolio.profile.application;

import com.brandonkamga.sourcefolio.profile.domain.model.CountryCode;
import com.brandonkamga.sourcefolio.profile.domain.model.DeveloperProfile;
import com.brandonkamga.sourcefolio.profile.domain.model.Role;
import com.brandonkamga.sourcefolio.profile.domain.model.Specialties;
import com.brandonkamga.sourcefolio.profile.domain.model.Specialty;
import com.brandonkamga.sourcefolio.profile.domain.port.ProfileStore;
import com.brandonkamga.sourcefolio.shared.exception.AppException;
import com.brandonkamga.sourcefolio.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private final List<ProfileChanged> announced = new ArrayList<>();
    private final ProfileCompletion profiles = new ProfileCompletionImpl(store, announced::add, Clock.fixed(NOW, ZoneOffset.UTC));

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
        profiles.answerSpecialties(MEMBER, List.of("fullstack"));

        assertThat(rows.get(MEMBER)).isEqualTo(new DeveloperProfile(MEMBER, new CountryCode("CM"), new Specialties(List.of(Specialty.FULLSTACK)), NOW));
    }

    @Test
    @DisplayName("an answer outside the lists is refused and changes nothing")
    void invalidAnswers() {
        profiles.answerCountry(MEMBER, "CM");

        assertThatThrownBy(() -> profiles.answerCountry(MEMBER, "Cameroun"))
                .isInstanceOf(AppException.class)
                .extracting(thrown -> ((AppException) thrown).messageKey()).isEqualTo("profile.country-invalid");
        assertThatThrownBy(() -> profiles.answerSpecialties(MEMBER, List.of("wizard")))
                .isInstanceOf(AppException.class)
                .extracting(thrown -> ((AppException) thrown).messageKey()).isEqualTo("profile.specialty-invalid");
        assertThat(rows.get(MEMBER).country()).isEqualTo(new CountryCode("CM"));
        assertThat(rows.get(MEMBER).specialties().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("ranked specialties keep the member's order and replace the earlier answer")
    void rankedSpecialties() {
        profiles.answerSpecialties(MEMBER, List.of("mobile"));
        profiles.answerSpecialties(MEMBER, List.of("backend", "devops", "frontend", "open-source"));

        assertThat(rows.get(MEMBER).specialties().slugs()).containsExactly("backend", "devops", "frontend", "open-source");
    }

    @Test
    @DisplayName("none, more than four, or the same interest twice is refused and changes nothing")
    void rankedSpecialtiesRefused() {
        profiles.answerSpecialties(MEMBER, List.of("mobile"));

        for (List<String> answer : List.of(List.<String>of(), List.of("backend", "devops", "frontend", "mobile", "cloud"), List.of("backend", "backend"))) {
            assertThatThrownBy(() -> profiles.answerSpecialties(MEMBER, answer))
                    .isInstanceOf(AppException.class)
                    .extracting(thrown -> ((AppException) thrown).messageKey()).isEqualTo("profile.specialties-invalid");
        }
        assertThat(rows.get(MEMBER).specialties().slugs()).containsExactly("mobile");
    }

    @Test
    @DisplayName("every saved answer announces the public profile; a refused one announces nothing")
    void announcesChanges() {
        profiles.answerCountry(MEMBER, "cm");
        profiles.answerSpecialties(MEMBER, List.of("backend", "devops"));
        try {
            profiles.answerSpecialties(MEMBER, List.of("wizard"));
        } catch (AppException refused) {
            // expected
        }

        assertThat(announced).containsExactly(
                new ProfileChanged(MEMBER, "CM", List.of(), null, null),
                new ProfileChanged(MEMBER, "CM", List.of("backend", "devops"), null, null));
    }

    @Test
    @DisplayName("the member declares one role, announced with the profile; a blank one removes it")
    void role() {
        profiles.answerRole(MEMBER, " devops ");
        assertThat(rows.get(MEMBER).role()).isEqualTo(Role.DEVOPS);
        assertThat(announced.getLast().role()).isEqualTo("devops");

        profiles.answerRole(MEMBER, "");
        assertThat(rows.get(MEMBER).role()).isNull();
        assertThat(announced.getLast().role()).isNull();
    }

    @Test
    @DisplayName("a role outside the list is refused and changes nothing")
    void roleRefused() {
        profiles.answerRole(MEMBER, "mobile");

        assertThatThrownBy(() -> profiles.answerRole(MEMBER, "wizard"))
                .isInstanceOf(AppException.class)
                .extracting(thrown -> ((AppException) thrown).code()).isEqualTo(ErrorCode.BAD_REQUEST);
        assertThat(rows.get(MEMBER).role()).isEqualTo(Role.MOBILE);
    }

    @Test
    @DisplayName("the member writes their own headline, tidied onto one line; a blank one removes it")
    void headline() {
        profiles.answerHeadline(MEMBER, "  Front-end React\n à Douala ");
        assertThat(rows.get(MEMBER).headline().value()).isEqualTo("Front-end React à Douala");

        profiles.answerHeadline(MEMBER, "   ");
        assertThat(rows.get(MEMBER).headline()).isNull();
    }

    @Test
    @DisplayName("a headline over eighty characters is refused and changes nothing")
    void headlineRefused() {
        profiles.answerHeadline(MEMBER, "Backend Java");

        assertThatThrownBy(() -> profiles.answerHeadline(MEMBER, "x".repeat(81)))
                .isInstanceOf(AppException.class)
                .extracting(thrown -> ((AppException) thrown).messageKey()).isEqualTo("profile.headline-invalid");
        assertThat(rows.get(MEMBER).headline().value()).isEqualTo("Backend Java");
    }
}
