package com.brandonkamga.sourcefolio.account.application;

import java.util.UUID;

/** Published after every GitHub sign-in, first visit or not. Other modules react to it. */
public record MemberSignedIn(UUID memberId, String login) {
}
