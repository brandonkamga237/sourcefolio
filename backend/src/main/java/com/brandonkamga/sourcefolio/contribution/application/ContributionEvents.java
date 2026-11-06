package com.brandonkamga.sourcefolio.contribution.application;

/** How the use cases announce what happened, in the module's published language. */
public interface ContributionEvents {

    void analyzed(ContributionAnalyzed event);
}
