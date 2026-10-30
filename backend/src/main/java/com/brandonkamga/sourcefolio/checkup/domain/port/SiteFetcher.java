package com.brandonkamga.sourcefolio.checkup.domain.port;

import java.util.OptionalInt;

/** Opens a member's site the way a visitor would, redirects included. */
public interface SiteFetcher {

    /** The HTTP status of the final page; empty when the site cannot be reached or is refused. */
    OptionalInt statusOf(String url);
}
