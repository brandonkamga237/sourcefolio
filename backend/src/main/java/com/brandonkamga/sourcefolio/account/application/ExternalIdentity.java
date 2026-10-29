package com.brandonkamga.sourcefolio.account.application;

import com.brandonkamga.sourcefolio.account.domain.model.AuthProvider;
import com.brandonkamga.sourcefolio.shared.domain.Language;

/** Who the provider says signed in, and the language of the browser they came from. */
public record ExternalIdentity(AuthProvider provider, String providerUserId, String login, Language language) {
}
