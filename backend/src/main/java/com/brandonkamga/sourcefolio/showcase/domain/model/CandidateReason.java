package com.brandonkamga.sourcefolio.showcase.domain.model;

/**
 * Why a repository looks like the member's portfolio, strongest signal first: the order of the
 * values is the order candidates are shown in.
 */
public enum CandidateReason {
    /** The {login}.github.io repository: GitHub's own place for a personal site. */
    USER_PAGES,
    /** A repository named portfolio, site or me, with a site. */
    NAMED_REPOSITORY,
    /** Any other repository whose homepage field points to a site (often a product, not a portfolio). */
    HOMEPAGE,
    /** A repository published with GitHub Pages under the member's github.io address. */
    PROJECT_PAGES
}
