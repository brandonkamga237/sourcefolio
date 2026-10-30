package com.brandonkamga.sourcefolio.showcase.api;

import com.brandonkamga.sourcefolio.showcase.api.dto.MyShowcaseResponse;
import com.brandonkamga.sourcefolio.showcase.application.ShowcasePreview;
import io.swagger.v3.oas.annotations.Operation;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

// The file's type is read from its bytes by the use case; the name and the type the browser
// sends are ignored.
@RestController
class MyShowcasePreviewController {

    private final ShowcasePreview preview;

    MyShowcasePreviewController(ShowcasePreview preview) {
        this.preview = preview;
    }

    @PutMapping(path = "/api/me/showcase/preview/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(operationId = "changeMyPreviewImage")
    MyShowcaseResponse changeImage(Authentication authentication, @RequestParam("file") MultipartFile file) throws IOException {
        return MyShowcaseResponse.of(preview.changeImage(memberId(authentication), file.getBytes()));
    }

    @PutMapping(path = "/api/me/showcase/preview/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(operationId = "changeMyPreviewVideo")
    MyShowcaseResponse changeVideo(Authentication authentication, @RequestParam("file") MultipartFile file) throws IOException {
        return MyShowcaseResponse.of(preview.changeVideo(memberId(authentication), file.getBytes()));
    }

    @DeleteMapping("/api/me/showcase/preview/image")
    @Operation(operationId = "removeMyPreviewImage")
    MyShowcaseResponse removeImage(Authentication authentication) {
        return MyShowcaseResponse.of(preview.removeImage(memberId(authentication)));
    }

    @DeleteMapping("/api/me/showcase/preview/video")
    @Operation(operationId = "removeMyPreviewVideo")
    MyShowcaseResponse removeVideo(Authentication authentication) {
        return MyShowcaseResponse.of(preview.removeVideo(memberId(authentication)));
    }

    private static UUID memberId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
