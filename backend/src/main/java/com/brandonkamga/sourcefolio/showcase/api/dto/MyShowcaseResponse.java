package com.brandonkamga.sourcefolio.showcase.api.dto;

import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;
import java.time.Instant;
import java.util.Optional;

/** showcase is null before the member's first publication. */
public record MyShowcaseResponse(MyShowcase showcase) {

    public record MyShowcase(String id, String siteUrl, String repository, String repositoryUrl, String status,
                             Instant publishedAt) {
    }

    public static MyShowcaseResponse of(Optional<Showcase> showcase) {
        return new MyShowcaseResponse(showcase.map(MyShowcaseResponse::view).orElse(null));
    }

    public static MyShowcaseResponse of(Showcase showcase) {
        return new MyShowcaseResponse(view(showcase));
    }

    private static MyShowcase view(Showcase showcase) {
        return new MyShowcase(showcase.id().toString(), showcase.siteUrl().value(),
                showcase.sourceRepo().coordinates().toString(), showcase.sourceRepo().url(), showcase.status().name(),
                showcase.publishedAt());
    }
}
