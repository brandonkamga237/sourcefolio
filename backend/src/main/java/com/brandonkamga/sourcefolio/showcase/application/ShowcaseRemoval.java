package com.brandonkamga.sourcefolio.showcase.application;

import java.util.UUID;

/**
 * Taking a portfolio down for good: its likes and its catalogue card go with it. Changing
 * portfolio is the same step followed by a new publication.
 */
public interface ShowcaseRemoval {

    /** The member removes their portfolio; refused when they have none. */
    void withdraw(UUID memberId);

    /** Removes whatever the member has published, if anything: their account is being deleted. */
    void removeAllOf(UUID memberId);
}
