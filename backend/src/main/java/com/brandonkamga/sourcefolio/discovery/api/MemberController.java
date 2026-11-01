package com.brandonkamga.sourcefolio.discovery.api;

import com.brandonkamga.sourcefolio.discovery.api.dto.MemberResponse;
import com.brandonkamga.sourcefolio.discovery.application.Members;
import com.brandonkamga.sourcefolio.shared.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class MemberController {

    private final Members members;

    MemberController(Members members) {
        this.members = members;
    }

    @GetMapping("/api/members/{login}")
    @Operation(operationId = "getMember")
    MemberResponse member(@PathVariable String login) {
        return members.findByLogin(login).map(MemberResponse::of).orElseThrow(() -> new NotFoundException("member.not-found"));
    }
}
