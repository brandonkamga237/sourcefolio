package com.brandonkamga.sourcefolio.showcase.application;

import java.util.Optional;

public interface PublicShowcases {

    /**
     * The showcase behind a public address. Unavailable and to-fix showcases stay reachable by
     * their link (the page shows a banner); a removed one is gone for visitors.
     */
    Optional<PublicShowcase> findBySlug(String slug);
}
