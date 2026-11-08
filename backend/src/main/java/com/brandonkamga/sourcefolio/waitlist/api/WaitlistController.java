package com.brandonkamga.sourcefolio.waitlist.api;

import com.brandonkamga.sourcefolio.waitlist.api.dto.WaitlistRequest;
import com.brandonkamga.sourcefolio.waitlist.application.Waitlist;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class WaitlistController {

    private final Waitlist waitlist;

    WaitlistController(Waitlist waitlist) {
        this.waitlist = waitlist;
    }

    @PostMapping("/api/waitlist")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "joinWaitlist")
    void join(@RequestBody WaitlistRequest request) {
        waitlist.join(request.toCommand());
    }
}
