package com.brandonkamga.sourcefolio.showcase.api;

import com.brandonkamga.sourcefolio.shared.exception.BadRequestException;
import com.brandonkamga.sourcefolio.shared.security.SecurityTestConfig;
import com.brandonkamga.sourcefolio.showcase.application.ShowcasePreview;
import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateReason;
import com.brandonkamga.sourcefolio.showcase.domain.model.PreviewMedia;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseCandidate;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseSlug;
import com.brandonkamga.sourcefolio.showcase.domain.model.SiteUrl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MyShowcasePreviewController.class)
@Import(SecurityTestConfig.class)
class MyShowcasePreviewControllerTest {

    private static final UUID MEMBER = UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11");
    private static final Showcase WITH_IMAGE = Showcase.publishVerified(MEMBER, new ShowcaseSlug("nekwalla"), new ShowcaseCandidate(
            new RepositoryCoordinates("nekwalla", "nekwalla.github.io"), new SiteUrl("https://nekwalla.github.io/"),
            CandidateReason.USER_PAGES), Instant.parse("2026-10-30T09:00:00Z")).stored()
            .withPreviewImage(new PreviewMedia("previews/x/0.jpg", "https://media.test/previews/x/0.jpg"));

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ShowcasePreview preview;

    @Test
    @DisplayName("an image sent as a file answers the showcase with its preview URL")
    void changeImage() throws Exception {
        when(preview.changeImage(eq(MEMBER), any())).thenReturn(WITH_IMAGE);

        mvc.perform(multipart("/api/me/showcase/preview/image").file(new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[]{1}))
                        .with(request -> { request.setMethod("PUT"); return request; }).with(member()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.showcase.previewImageUrl").value("https://media.test/previews/x/0.jpg"))
                .andExpect(jsonPath("$.showcase.previewVideoUrl").doesNotExist());
    }

    @Test
    @DisplayName("a refused file is a 400 in the member's language")
    void refused() throws Exception {
        when(preview.changeImage(eq(MEMBER), any())).thenThrow(new BadRequestException("preview.image-invalid"));

        mvc.perform(multipart("/api/me/showcase/preview/image").file(new MockMultipartFile("file", "a.svg", "image/svg+xml", new byte[]{1}))
                        .with(request -> { request.setMethod("PUT"); return request; }).with(member()).with(csrf()).header("Accept-Language", "fr"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("L’image doit être un fichier JPEG, PNG ou WebP de 5 Mo maximum."));
    }

    @Test
    @DisplayName("a form without its file is a 400, and a visitor cannot send a preview")
    void missingFileAndVisitor() throws Exception {
        mvc.perform(multipart("/api/me/showcase/preview/video").with(request -> { request.setMethod("PUT"); return request; })
                        .with(member()).with(csrf()))
                .andExpect(status().isBadRequest());
        mvc.perform(multipart("/api/me/showcase/preview/image").file(new MockMultipartFile("file", new byte[]{1}))
                        .with(request -> { request.setMethod("PUT"); return request; }).with(csrf()))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(preview);
    }

    private static RequestPostProcessor member() {
        return oauth2Login().oauth2User(new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_MEMBER")), Map.of("id", MEMBER.toString()), "id"));
    }
}
