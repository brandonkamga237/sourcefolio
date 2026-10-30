package com.brandonkamga.sourcefolio.showcase.api;

import com.brandonkamga.sourcefolio.showcase.api.dto.ShowcaseCandidatesResponse;
import com.brandonkamga.sourcefolio.showcase.application.ShowcaseDetection;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
class ShowcaseCandidatesController {

    private final ShowcaseDetection detection;

    ShowcaseCandidatesController(ShowcaseDetection detection) {
        this.detection = detection;
    }

    @GetMapping("/api/me/showcase-candidates")
    @Operation(operationId = "getMyShowcaseCandidates")
    ShowcaseCandidatesResponse candidates(Authentication authentication) {
        return ShowcaseCandidatesResponse.of(detection.findCandidates(UUID.fromString(authentication.getName())));
    }
}
