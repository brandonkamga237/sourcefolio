package com.brandonkamga.sourcefolio.discovery.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * What the community sees of a member who has published: their public profile, since when they
 * are on SourceFolio, the likes their work received, and their portfolio's card.
 */
public record MemberCard(String login, String countryCode, List<String> specialties, String headline, String role,
                         Instant onSourceFolioSince, int likesReceived, CatalogEntry showcase) {

    public MemberCard {
        Objects.requireNonNull(login, "login");
        Objects.requireNonNull(showcase, "showcase");
        specialties = List.copyOf(specialties);
    }
}
