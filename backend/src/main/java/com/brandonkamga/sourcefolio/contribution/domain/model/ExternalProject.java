package com.brandonkamga.sourcefolio.contribution.domain.model;

/** A repository someone else owns, and how many of the member's pull requests it merged. */
public record ExternalProject(String nameWithOwner, int mergedPullRequests) {
}
