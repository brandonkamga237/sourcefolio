package com.brandonkamga.sourcefolio.discovery.api.dto;

import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogEntry;
import com.brandonkamga.sourcefolio.discovery.domain.model.MemberCard;
import java.time.Instant;
import java.util.List;

/** A published member as anyone sees them. No ids: the login is the public handle. */
public record MemberResponse(String login, String githubUrl, String countryCode, List<String> specialties, String headline,
                             String role, Instant onSourceFolioSince, int likesReceived, Showcase showcase) {

    public record Showcase(String slug, String siteUrl, String repository, String repositoryUrl, String previewImageUrl,
                           Integer repositoryStars, int likes, Instant publishedAt) {

        static Showcase of(CatalogEntry entry) {
            return new Showcase(entry.slug(), entry.siteUrl(), entry.repository(), entry.repositoryUrl(), entry.previewImageUrl(),
                    entry.repositoryStars(), entry.likes(), entry.publishedAt());
        }
    }

    public static MemberResponse of(MemberCard card) {
        return new MemberResponse(card.login(), "https://github.com/" + card.login(), card.countryCode(), card.specialties(), card.headline(),
                card.role(), card.onSourceFolioSince(), card.likesReceived(), Showcase.of(card.showcase()));
    }
}
