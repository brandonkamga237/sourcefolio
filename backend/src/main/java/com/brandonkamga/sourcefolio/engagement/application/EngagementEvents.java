package com.brandonkamga.sourcefolio.engagement.application;

/** How the use cases announce what happened, in the module's published language. */
public interface EngagementEvents {

    void likesChanged(ShowcaseLikesChanged event);
}
