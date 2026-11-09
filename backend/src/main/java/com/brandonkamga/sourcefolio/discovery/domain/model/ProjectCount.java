package com.brandonkamga.sourcefolio.discovery.domain.model;

/** A repository, and how many listed profiles have a pull request merged there. */
public record ProjectCount(String repository, long contributors) {
}
