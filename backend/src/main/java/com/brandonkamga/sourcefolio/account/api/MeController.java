package com.brandonkamga.sourcefolio.account.api;

import com.brandonkamga.sourcefolio.account.api.dto.MeResponse;
import com.brandonkamga.sourcefolio.account.application.AccountDeletion;
import com.brandonkamga.sourcefolio.account.application.SignInService;
import com.brandonkamga.sourcefolio.account.domain.model.UserId;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
class MeController {

    private final SignInService members;
    private final AccountDeletion deletion;
    private final FindByIndexNameSessionRepository<?> sessions;

    MeController(SignInService members, AccountDeletion deletion, FindByIndexNameSessionRepository<?> sessions) {
        this.members = members;
        this.deletion = deletion;
        this.sessions = sessions;
    }

    // 401 for a visitor (from the security chain): the frontend shows its visitor screens on it.
    @GetMapping("/api/me")
    @Operation(operationId = "getMe")
    MeResponse me(Authentication authentication) {
        return MeResponse.of(members.requireMember(memberId(authentication)));
    }

    // Signs the member out everywhere too: a session left open on another phone would point to
    // an account that no longer exists.
    @DeleteMapping("/api/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteMyAccount")
    void delete(Authentication authentication, HttpServletRequest request) {
        deletion.delete(memberId(authentication));
        HttpSession current = request.getSession(false);
        if (current != null) {
            current.invalidate();
        }
        sessions.findByPrincipalName(authentication.getName()).keySet().forEach(sessions::deleteById);
        SecurityContextHolder.clearContext();
    }

    private static UserId memberId(Authentication authentication) {
        return new UserId(UUID.fromString(authentication.getName()));
    }
}
