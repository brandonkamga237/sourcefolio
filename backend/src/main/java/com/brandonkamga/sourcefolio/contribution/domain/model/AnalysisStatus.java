package com.brandonkamga.sourcefolio.contribution.domain.model;

public enum AnalysisStatus {
    /** Asked for, not read yet. */
    PENDING,
    /** A snapshot exists; it may be refreshing. */
    READY,
    /** No snapshot exists, and GitHub did not know the login or the reading never came back. */
    FAILED
}
