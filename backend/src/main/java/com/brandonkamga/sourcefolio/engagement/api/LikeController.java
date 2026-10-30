package com.brandonkamga.sourcefolio.engagement.api;

import com.brandonkamga.sourcefolio.engagement.api.dto.LikeResponse;
import com.brandonkamga.sourcefolio.engagement.application.Likes;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class LikeController {

    private final Likes likes;

    LikeController(Likes likes) {
        this.likes = likes;
    }

    @GetMapping("/api/showcases/{slug}/likes")
    @Operation(operationId = "getShowcaseLikes")
    LikeResponse likes(@PathVariable String slug, Authentication authentication) {
        return LikeResponse.of(likes.findState(memberIdOrNull(authentication), slug));
    }

    // PUT and DELETE rather than a toggle: a repeated request leaves the same state.
    @PutMapping("/api/showcases/{slug}/like")
    @Operation(operationId = "likeShowcase")
    LikeResponse like(@PathVariable String slug, Authentication authentication) {
        return LikeResponse.of(likes.like(UUID.fromString(authentication.getName()), slug));
    }

    @DeleteMapping("/api/showcases/{slug}/like")
    @Operation(operationId = "unlikeShowcase")
    LikeResponse unlike(@PathVariable String slug, Authentication authentication) {
        return LikeResponse.of(likes.unlike(UUID.fromString(authentication.getName()), slug));
    }

    private static UUID memberIdOrNull(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken
                ? null
                : UUID.fromString(authentication.getName());
    }
}
