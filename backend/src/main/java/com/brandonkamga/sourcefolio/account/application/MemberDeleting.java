package com.brandonkamga.sourcefolio.account.application;

import java.util.UUID;

/**
 * A member asked for their account to be deleted (spec: their comments go, their likes are
 * taken back). Delivered inside the deletion's transaction, before the account row goes:
 * each module removes what it owns and announces what changed, and a failure anywhere rolls
 * the whole deletion back rather than leaving half an account.
 */
public record MemberDeleting(UUID memberId) {
}
