package com.brandonkamga.sourcefolio.discovery.api.dto;

import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogEntry;
import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogPage;
import java.time.Instant;
import java.util.List;

/**
 * One page of cards. Like counts and the repository's GitHub stars are shown on each card;
 * neither ever orders Discover, which stays shuffled (only the Ranking orders by likes).
 */
public record CatalogResponse(List<Card> items, String nextCursor, long total) {

    public record Card(String slug, String siteUrl, String repository, String repositoryUrl, String ownerLogin,
                       Instant publishedAt, String previewImageUrl, String previewVideoUrl, Integer repositoryStars,
                       int likes, String countryCode, List<String> specialties, String headline,
                       String repositoryDescription, List<String> repositoryLanguages, Instant repositoryPushedAt) {

        static Card of(CatalogEntry entry) {
            return new Card(entry.slug(), entry.siteUrl(), entry.repository(), entry.repositoryUrl(), entry.ownerLogin(),
                    entry.publishedAt(), entry.previewImageUrl(), entry.previewVideoUrl(), entry.repositoryStars(), entry.likes(),
                    entry.countryCode(), entry.specialties(), entry.headline(), entry.repositoryDescription(),
                    entry.repositoryLanguages(), entry.repositoryPushedAt());
        }
    }

    public static CatalogResponse of(CatalogPage page) {
        return new CatalogResponse(page.entries().stream().map(Card::of).toList(), page.nextCursor(), page.total());
    }
}
