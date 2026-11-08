package com.brandonkamga.sourcefolio.contribution.api.dto;

import com.brandonkamga.sourcefolio.contribution.application.ProfileCard;

/**
 * A published profile. country is an ISO code; city, bio and linkedInUrl are the member's own
 * words, shown but never ranked. indicators carries every figure with its GitHub proof.
 */
public record ProfileCardResponse(String login, String name, String country, String city, String bio, String linkedInUrl,
                                  AnalysisResponse.Indicators indicators) {

    public static ProfileCardResponse of(ProfileCard card) {
        return new ProfileCardResponse(card.login(), card.snapshot().name(), card.profile().countryCode(), card.profile().city(),
                card.profile().bio(), card.profile().linkedInUrl(), AnalysisResponse.Indicators.of(card.login(), card.snapshot()));
    }
}
