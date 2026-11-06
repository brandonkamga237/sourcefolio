package com.brandonkamga.sourcefolio.contribution.domain.model;

public enum AnalysisStatus {
    /** Asked for, not read yet. */
    PENDING,
    /** A snapshot exists; it may be refreshing. */
    READY,
    /** GitHub did not know the login, and no earlier snapshot exists. */
    FAILED
}
