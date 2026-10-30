package com.brandonkamga.sourcefolio.profile.application;

import com.brandonkamga.sourcefolio.profile.domain.model.DeveloperProfile;
import java.util.UUID;

/** The one-question-at-a-time profile: each answer is saved on its own. */
public interface ProfileCompletion {

    /** The member's answers so far; a blank profile before the first one. */
    DeveloperProfile findMyProfile(UUID memberId);

    DeveloperProfile answerCountry(UUID memberId, String countryCode);

    DeveloperProfile answerSpecialty(UUID memberId, String specialtySlug);
}
