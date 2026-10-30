package com.brandonkamga.sourcefolio.showcase.application;

import java.util.List;
import java.util.UUID;

/** Keeps a copy of the GitHub stars of each showcase's repository, read at most once a day. */
public interface RepositoryStars {

    /** The showcases whose stars are due, least recently read first. */
    List<UUID> listDue(int limit);

    /** Reads the stars of one showcase's repository; safe to run twice. */
    void refresh(UUID showcaseId);
}
