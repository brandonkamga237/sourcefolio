package com.brandonkamga.sourcefolio.showcase.api;

import com.brandonkamga.sourcefolio.showcase.api.dto.MyShowcaseResponse;
import com.brandonkamga.sourcefolio.showcase.api.dto.PublishShowcaseRequest;
import com.brandonkamga.sourcefolio.showcase.application.ShowcasePublication;
import com.brandonkamga.sourcefolio.showcase.application.ShowcaseRemoval;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class MyShowcaseController {

    private final ShowcasePublication publication;
    private final ShowcaseRemoval removal;

    MyShowcaseController(ShowcasePublication publication, ShowcaseRemoval removal) {
        this.publication = publication;
        this.removal = removal;
    }

    @GetMapping("/api/me/showcase")
    @Operation(operationId = "getMyShowcase")
    MyShowcaseResponse myShowcase(Authentication authentication) {
        return MyShowcaseResponse.of(publication.findMyShowcase(memberId(authentication)));
    }

    // Checks the site before answering: a few seconds at most (the fetch times out at 10).
    @PostMapping("/api/me/showcase")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "publishMyShowcase")
    MyShowcaseResponse publish(Authentication authentication, @Valid @RequestBody PublishShowcaseRequest request) {
        return MyShowcaseResponse.of(publication.publish(request.toCommand(memberId(authentication))));
    }

    // Also the first half of changing portfolio: the member then publishes another repository.
    @DeleteMapping("/api/me/showcase")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "withdrawMyShowcase")
    void withdraw(Authentication authentication) {
        removal.withdraw(memberId(authentication));
    }

    private static UUID memberId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
