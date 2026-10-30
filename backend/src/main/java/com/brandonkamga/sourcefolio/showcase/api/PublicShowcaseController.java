package com.brandonkamga.sourcefolio.showcase.api;

import com.brandonkamga.sourcefolio.shared.exception.NotFoundException;
import com.brandonkamga.sourcefolio.showcase.api.dto.PublicShowcaseResponse;
import com.brandonkamga.sourcefolio.showcase.application.PublicShowcases;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PublicShowcaseController {

    private final PublicShowcases showcases;

    PublicShowcaseController(PublicShowcases showcases) {
        this.showcases = showcases;
    }

    @GetMapping("/api/showcases/{slug}")
    @Operation(operationId = "getShowcase")
    PublicShowcaseResponse showcase(@PathVariable String slug) {
        return showcases.findBySlug(slug).map(PublicShowcaseResponse::of)
                .orElseThrow(() -> new NotFoundException("showcase.not-found"));
    }
}
