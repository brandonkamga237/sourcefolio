package com.brandonkamga.sourcefolio.profile.api;

import com.brandonkamga.sourcefolio.profile.api.dto.CountryAnswer;
import com.brandonkamga.sourcefolio.profile.api.dto.HeadlineAnswer;
import com.brandonkamga.sourcefolio.profile.api.dto.ProfileResponse;
import com.brandonkamga.sourcefolio.profile.api.dto.RoleAnswer;
import com.brandonkamga.sourcefolio.profile.api.dto.SpecialtiesAnswer;
import com.brandonkamga.sourcefolio.profile.application.ProfileCompletion;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.Authentication;
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

    private static UUID memberId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
