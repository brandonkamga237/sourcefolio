package com.brandonkamga.sourcefolio.discovery.domain.model;

/** Optional filters: an ISO country code and a GitHub language name; null means all. */
public record RankingFilter(String countryCode, String language) {
}
