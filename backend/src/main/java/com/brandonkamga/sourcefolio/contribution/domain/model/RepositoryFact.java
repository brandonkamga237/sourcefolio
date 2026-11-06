package com.brandonkamga.sourcefolio.contribution.domain.model;

/** One of the member's most starred repositories, as the profile shows it. */
public record RepositoryFact(String name, String description, String url, int stars, String primaryLanguage) {
}
