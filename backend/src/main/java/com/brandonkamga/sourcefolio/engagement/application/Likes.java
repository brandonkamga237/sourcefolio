package com.brandonkamga.sourcefolio.engagement.application;

import java.util.UUID;

/** Likes on a showcase, found by its public address. Counts are public (Ranking tab). */
public interface Likes {

    /** The count, and whether {@code memberId} likes it; memberId is null for a visitor. */
    LikeState findState(UUID memberId, String slug);

    /** Likes it; liking twice changes nothing. A member cannot like their own showcase. */
    LikeState like(UUID memberId, String slug);

    /** Takes the like back; doing it twice changes nothing. */
    LikeState unlike(UUID memberId, String slug);

    /** Takes back every like the member gave, and announces the new counts: their account is going. */
    void withdrawAllBy(UUID memberId);
}
