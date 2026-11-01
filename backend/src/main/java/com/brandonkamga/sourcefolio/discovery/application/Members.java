package com.brandonkamga.sourcefolio.discovery.application;

import com.brandonkamga.sourcefolio.discovery.domain.model.MemberCard;
import java.util.Optional;

/** The open community: anyone may see who a published member is. */
public interface Members {

    /** Empty when nobody with this login has a portfolio in the catalogue. */
    Optional<MemberCard> findByLogin(String login);
}
