package com.brandonkamga.sourcefolio.account.api;

import com.brandonkamga.sourcefolio.account.api.dto.EmailRequest;
import com.brandonkamga.sourcefolio.account.api.dto.EmailResponse;
import com.brandonkamga.sourcefolio.account.application.AccountSettings;
import com.brandonkamga.sourcefolio.account.domain.model.UserId;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class EmailController {

    private final AccountSettings settings;

    EmailController(AccountSettings settings) {
        this.settings = settings;
    }

    @GetMapping("/api/me/email")
    @Operation(operationId = "getMyEmail")
    EmailResponse email(Authentication authentication) {
        return EmailResponse.of(settings.findEmail(memberId(authentication)));
    }

    @PutMapping("/api/me/email")
    @Operation(operationId = "changeMyEmail")
    EmailResponse change(Authentication authentication, @RequestBody EmailRequest request) {
        return EmailResponse.of(settings.changeEmail(request.toCommand(memberId(authentication))));
    }

    private static UserId memberId(Authentication authentication) {
        return new UserId(UUID.fromString(authentication.getName()));
    }
}
