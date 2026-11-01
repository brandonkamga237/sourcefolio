package com.brandonkamga.sourcefolio.profile.application;

/** How the use cases announce what happened, in the module's published language. */
public interface ProfileEvents {

    void profileChanged(ProfileChanged event);
}
