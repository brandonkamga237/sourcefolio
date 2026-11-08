package com.brandonkamga.sourcefolio.profile.api;

import com.brandonkamga.sourcefolio.profile.api.dto.BioAnswer;
import com.brandonkamga.sourcefolio.profile.api.dto.CityAnswer;
import com.brandonkamga.sourcefolio.profile.api.dto.CountryAnswer;
import com.brandonkamga.sourcefolio.profile.api.dto.HeadlineAnswer;
import com.brandonkamga.sourcefolio.profile.api.dto.LinkedInAnswer;
import com.brandonkamga.sourcefolio.profile.api.dto.ProfileResponse;
import com.brandonkamga.sourcefolio.profile.api.dto.RoleAnswer;
import com.brandonkamga.sourcefolio.profile.api.dto.SpecialtiesAnswer;
import com.brandonkamga.sourcefolio.profile.application.ProfileCompletion;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// One route per question: each answer is saved as soon as it is given.
@RestController
class ProfileController {

    private final ProfileCompletion profiles;

    ProfileController(ProfileCompletion profiles) {
        this.profiles = profiles;
    }

    @GetMapping("/api/me/profile")
    @Operation(operationId = "getMyProfile")
    ProfileResponse profile(Authentication authentication) {
        return ProfileResponse.of(profiles.findMyProfile(memberId(authentication)));
    }

    @PutMapping("/api/me/profile/country")
    @Operation(operationId = "answerMyCountry")
    ProfileResponse country(Authentication authentication, @Valid @RequestBody CountryAnswer answer) {
        return ProfileResponse.of(profiles.answerCountry(memberId(authentication), answer.country()));
    }

    @PutMapping("/api/me/profile/specialties")
    @Operation(operationId = "answerMySpecialties")
    ProfileResponse specialties(Authentication authentication, @Valid @RequestBody SpecialtiesAnswer answer) {
        return ProfileResponse.of(profiles.answerSpecialties(memberId(authentication), answer.specialties()));
    }

    @PutMapping("/api/me/profile/headline")
    @Operation(operationId = "answerMyHeadline")
    ProfileResponse headline(Authentication authentication, @RequestBody HeadlineAnswer answer) {
        return ProfileResponse.of(profiles.answerHeadline(memberId(authentication), answer.headline()));
    }

    @PutMapping("/api/me/profile/role")
    @Operation(operationId = "answerMyRole")
    ProfileResponse role(Authentication authentication, @RequestBody RoleAnswer answer) {
        return ProfileResponse.of(profiles.answerRole(memberId(authentication), answer.role()));
    }

    @PutMapping("/api/me/profile/city")
    @Operation(operationId = "answerMyCity")
    ProfileResponse city(Authentication authentication, @RequestBody CityAnswer answer) {
        return ProfileResponse.of(profiles.answerCity(memberId(authentication), answer.city()));
    }

    @PutMapping("/api/me/profile/bio")
    @Operation(operationId = "answerMyBio")
    ProfileResponse bio(Authentication authentication, @RequestBody BioAnswer answer) {
        return ProfileResponse.of(profiles.answerBio(memberId(authentication), answer.bio()));
    }

    @PutMapping("/api/me/profile/linkedin")
    @Operation(operationId = "answerMyLinkedIn")
    ProfileResponse linkedIn(Authentication authentication, @RequestBody LinkedInAnswer answer) {
        return ProfileResponse.of(profiles.answerLinkedIn(memberId(authentication), answer.linkedIn()));
    }

    // Publishing is the member's explicit consent; DELETE takes the profile back at once.
    @PutMapping("/api/me/profile/publication")
    @Operation(operationId = "publishMyProfile")
    ProfileResponse publish(Authentication authentication) {
        return ProfileResponse.of(profiles.publish(memberId(authentication)));
    }

    @DeleteMapping("/api/me/profile/publication")
    @Operation(operationId = "unpublishMyProfile")
    ProfileResponse unpublish(Authentication authentication) {
        return ProfileResponse.of(profiles.unpublish(memberId(authentication)));
    }

    private static UUID memberId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
