package com.brandonkamga.sourcefolio.contribution.api;

import com.brandonkamga.sourcefolio.contribution.api.dto.AnalysisResponse;
import com.brandonkamga.sourcefolio.contribution.api.dto.ProfileCardResponse;
import com.brandonkamga.sourcefolio.contribution.application.ContributionAnalysis;
import com.brandonkamga.sourcefolio.contribution.application.ContributionReports;
import com.brandonkamga.sourcefolio.contribution.application.PublicProfiles;
import com.brandonkamga.sourcefolio.shared.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ContributionController {

    private final ContributionReports reports;
    private final ContributionAnalysis analysis;
    private final PublicProfiles profiles;

    ContributionController(ContributionReports reports, ContributionAnalysis analysis, PublicProfiles profiles) {
        this.reports = reports;
        this.analysis = analysis;
        this.profiles = profiles;
    }

    // The same 404 for an unknown login and an unpublished profile: a private profile must not be
    // detectable from outside.
    @GetMapping("/api/profiles/{login}")
    @Operation(operationId = "getProfile")
    ProfileCardResponse profile(@PathVariable String login) {
        return profiles.findByLogin(login).map(ProfileCardResponse::of).orElseThrow(() -> new NotFoundException("profile.not-found"));
    }

    @GetMapping("/api/me/analysis")
    @Operation(operationId = "getMyAnalysis")
    AnalysisResponse mine(Authentication authentication) {
        return reports.findMine(memberId(authentication)).map(AnalysisResponse::of).orElseGet(AnalysisResponse::none);
    }

    // For members who signed in before analyses existed, and for the "analyse again" button; a
    // fresh reading is not repeated.
    @PostMapping("/api/me/analysis")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(operationId = "requestMyAnalysis")
    void request(Authentication authentication) {
        String login = ((OAuth2AuthenticatedPrincipal) authentication.getPrincipal()).getAttribute("login");
        analysis.requestAnalysis(memberId(authentication), login);
    }

    private static UUID memberId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
