package com.brandonkamga.sourcefolio.showcase.domain.port;

import java.util.UUID;

/** What this module needs to know about members, who belong to the account module. */
public interface MemberDirectory {

    /** The member's current GitHub login. */
    String loginOf(UUID memberId);
}
