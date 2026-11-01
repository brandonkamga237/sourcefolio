package com.brandonkamga.sourcefolio.engagement.api;

import com.brandonkamga.sourcefolio.engagement.api.dto.ReceivedFeedbackResponse;
import com.brandonkamga.sourcefolio.engagement.api.dto.UnreadResponse;
import com.brandonkamga.sourcefolio.engagement.application.FeedbackInbox;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class FeedbackInboxController {

    private final FeedbackInbox inbox;

    FeedbackInboxController(FeedbackInbox inbox) {
        this.inbox = inbox;
    }

    @GetMapping("/api/me/feedback")
    @Operation(operationId = "getMyReceivedFeedback")
    ReceivedFeedbackResponse received(@RequestParam(required = false) String cursor, @RequestParam(defaultValue = "10") int size,
                                      Authentication authentication) {
        UUID memberId = memberId(authentication);
        return ReceivedFeedbackResponse.of(inbox.findReceived(memberId, cursor, size), memberId);
    }

    @GetMapping("/api/me/feedback/unread")
    @Operation(operationId = "countMyUnreadFeedback")
    UnreadResponse unread(Authentication authentication) {
        return new UnreadResponse(inbox.countUnread(memberId(authentication)));
    }

    // PUT: reading twice leaves the same state.
    @PutMapping("/api/me/feedback/seen")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "markMyFeedbackSeen")
    void seen(Authentication authentication) {
        inbox.markSeen(memberId(authentication));
    }

    private static UUID memberId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
