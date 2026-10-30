package com.brandonkamga.sourcefolio.account.application;

import com.brandonkamga.sourcefolio.account.domain.model.UserId;
import com.brandonkamga.sourcefolio.shared.domain.Language;

/** What other modules and the web layer may know about a signed-in member. */
public record Member(UserId id, String login, Language language) {
}
