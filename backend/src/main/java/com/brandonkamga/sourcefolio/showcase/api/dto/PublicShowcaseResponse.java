package com.brandonkamga.sourcefolio.showcase.api.dto;

import com.brandonkamga.sourcefolio.showcase.application.PublicShowcase;
import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;
import java.time.Instant;
import java.util.List;

/** What a visitor sees of a showcase: no owner id, no history. */
public record PublicShowcaseResponse(String slug, String siteUrl, String repository, String repositoryUrl,
                                     String status, Instant publishedAt, String ownerLogin, String ownerUrl,
                                     String previewImageUrl, String previewVideoUrl, Integer repositoryStars,
                                     String repositoryDescription, List<String> repositoryLanguages, Instant repositoryPushedAt) {

    public static PublicShowcaseResponse of(PublicShowcase found) {
        Showcase showcase = found.showcase();
        return new PublicShowcaseResponse(showcase.slug().value(), showcase.siteUrl().value(),
                showcase.sourceRepo().coordinates().toString(), showcase.sourceRepo().url(), showcase.status().name(),
                showcase.publishedAt(), found.ownerLogin(), "https://github.com/" + found.ownerLogin(),
                showcase.previewImage() == null ? null : showcase.previewImage().url(),
                showcase.previewVideo() == null ? null : showcase.previewVideo().url(), showcase.sourceRepo().stars(),
                showcase.sourceRepo().description(), showcase.sourceRepo().languages(), showcase.sourceRepo().pushedAt());
    }
}
