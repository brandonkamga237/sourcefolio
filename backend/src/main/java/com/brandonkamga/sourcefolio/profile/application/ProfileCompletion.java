package com.brandonkamga.sourcefolio.profile.application;

import com.brandonkamga.sourcefolio.profile.domain.model.DeveloperProfile;
import java.util.List;
import java.util.UUID;

/** The one-question-at-a-time profile: each answer is saved on its own. */
public interface ProfileCompletion {

    /** The member's answers so far; a blank profile before the first one. */
    DeveloperProfile findMyProfile(UUID memberId);

    DeveloperProfile answerCountry(UUID memberId, String countryCode);

    /** One to three specialty slugs, most important first; replaces the earlier answer. */
    DeveloperProfile answerSpecialties(UUID memberId, List<String> specialtySlugs);

    /** The member's own line; blank or null removes it. */
    DeveloperProfile answerHeadline(UUID memberId, String headline);

    /** The one role the member says they work in, as a slug; blank or null removes it. */
    DeveloperProfile answerRole(UUID memberId, String roleSlug);

    /** The city shown on the profile and used by the map; blank or null removes it. */
    DeveloperProfile answerCity(UUID memberId, String city);

    /** A few words about the member, up to 160 characters; blank or null removes it. */
    DeveloperProfile answerBio(UUID memberId, String bio);

    /** A LinkedIn handle or profile URL, kept as the handle; blank or null removes it. */
    DeveloperProfile answerLinkedIn(UUID memberId, String linkedIn);

    /** Makes the profile public; it takes a country. Publishing twice changes nothing. */
    DeveloperProfile publish(UUID memberId);

    /** Makes the profile private again, at once. */
    DeveloperProfile unpublish(UUID memberId);
}
