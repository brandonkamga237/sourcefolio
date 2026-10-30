package com.brandonkamga.sourcefolio.discovery.api.dto;

import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogEntry;
import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogPage;
import java.time.Instant;
import java.util.List;

/** One page of cards. No count of likes or views: the catalogue never ranks by popularity. */
public record CatalogResponse(List<Card> items, String nextCursor) {

    public record Card(String slug, String siteUrl, String repository, String repositoryUrl, String ownerLogin,
                       Instant publishedAt) {

        static Card of(CatalogEntry entry) {
            return new Card(entry.slug(), entry.siteUrl(), entry.repository(), entry.repositoryUrl(), entry.ownerLogin(),
                    entry.publishedAt());
        }
    }

    public static CatalogResponse of(CatalogPage page) {
        return new CatalogResponse(page.entries().stream().map(Card::of).toList(), page.nextCursor());
    }
}
