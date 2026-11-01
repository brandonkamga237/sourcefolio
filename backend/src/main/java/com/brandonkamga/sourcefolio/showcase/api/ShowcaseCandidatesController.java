package com.brandonkamga.sourcefolio.showcase.api;

import com.brandonkamga.sourcefolio.showcase.api.dto.ShowcaseCandidatesResponse;
import com.brandonkamga.sourcefolio.showcase.application.ShowcaseDetection;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    // 202: the search runs in the background; the page reads the candidates again until
    // scannedAt moves.
    @PostMapping("/api/me/showcase-candidates/rescan")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(operationId = "rescanMyShowcaseCandidates")
    void rescan(Authentication authentication) {
        detection.requestRescan(UUID.fromString(authentication.getName()));
    }
}
