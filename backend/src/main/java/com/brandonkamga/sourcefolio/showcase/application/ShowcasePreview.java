package com.brandonkamga.sourcefolio.showcase.application;

import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;
import java.util.UUID;

/** The author's choice of preview for their card. Stores files in the bucket: no transaction around it. */
public interface ShowcasePreview {

    /** Replaces the preview image with a JPEG, PNG or WebP of 5 MB at most. */
    Showcase changeImage(UUID memberId, byte[] content);

    /** Replaces the preview video with an MP4 of 8 MB and 15 seconds at most. */
    Showcase changeVideo(UUID memberId, byte[] content);

    /** Back to the automatic preview. */
    Showcase removeImage(UUID memberId);

    Showcase removeVideo(UUID memberId);
}
