package com.brandonkamga.sourcefolio.engagement.api;

import com.brandonkamga.sourcefolio.engagement.api.dto.FeedbackPageResponse;
import com.brandonkamga.sourcefolio.engagement.api.dto.FeedbackRequest;
import com.brandonkamga.sourcefolio.engagement.api.dto.FeedbackResponse;
import com.brandonkamga.sourcefolio.engagement.application.Feedbacks;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class FeedbackController {

    private final Feedbacks feedbacks;

    FeedbackController(Feedbacks feedbacks) {
        this.feedbacks = feedbacks;
    }

    @GetMapping("/api/showcases/{slug}/feedback")
    @Operation(operationId = "listShowcaseFeedback")
    FeedbackPageResponse list(@PathVariable String slug, @RequestParam(required = false) String cursor,
                              @RequestParam(defaultValue = "10") int size, Authentication authentication) {
        return FeedbackPageResponse.of(feedbacks.list(slug, cursor, size), memberIdOrNull(authentication));
    }

    @PostMapping("/api/showcases/{slug}/feedback")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "postShowcaseFeedback")
    FeedbackResponse post(@PathVariable String slug, @RequestBody FeedbackRequest request, Locale locale,
                          Authentication authentication) {
        UUID memberId = UUID.fromString(authentication.getName());
        return FeedbackResponse.of(feedbacks.post(memberId, slug, request.angle(), request.body(), request.toLanguage(locale)),
                memberId);
    }

    @DeleteMapping("/api/feedback/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteFeedback")
    void delete(@PathVariable UUID id, Authentication authentication) {
        feedbacks.delete(UUID.fromString(authentication.getName()), id);
    }

    private static UUID memberIdOrNull(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken
                ? null
                : UUID.fromString(authentication.getName());
    }
}
