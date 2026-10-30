package com.brandonkamga.sourcefolio.account.api;

import com.brandonkamga.sourcefolio.account.api.dto.MeResponse;
import com.brandonkamga.sourcefolio.account.application.SignInService;
import com.brandonkamga.sourcefolio.account.domain.model.UserId;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
class MeController {

    private final SignInService members;

    MeController(SignInService members) {
        this.members = members;
    }

    // 401 for a visitor (from the security chain): the frontend shows its visitor screens on it.
    @GetMapping("/api/me")
    @Operation(operationId = "getMe")
    MeResponse me(Authentication authentication) {
        return MeResponse.of(members.requireMember(new UserId(UUID.fromString(authentication.getName()))));
    }
}
