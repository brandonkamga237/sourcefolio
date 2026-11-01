package com.brandonkamga.sourcefolio.engagement.api.dto;

import com.brandonkamga.sourcefolio.shared.domain.Language;
import java.util.Locale;

/** The angle is optional; language is the page the member wrote on, the request's language otherwise. */
public record FeedbackRequest(String angle, String body, String language) {

    public Language toLanguage(Locale requestLocale) {
        return language == null || language.isBlank() ? Language.of(requestLocale) : Language.fromTag(language);
    }
}
