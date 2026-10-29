package com.brandonkamga.sourcefolio.account.application;

/**
 * How the use cases announce what happened. Lives beside the events it carries, in the module's
 * published language: other modules listen to these events, never to the module's domain.
 */
public interface AccountEvents {

    void memberSignedIn(MemberSignedIn event);
}
