package com.brandonkamga.sourcefolio.showcase.application;

/** How the use cases announce what happened, in the module's published language. */
public interface ShowcaseEvents {

    void showcasePublished(ShowcasePublished event);

    void previewChanged(ShowcasePreviewChanged event);

    void showcaseRemoved(ShowcaseRemoved event);

    void starsChanged(ShowcaseStarsChanged event);
}
