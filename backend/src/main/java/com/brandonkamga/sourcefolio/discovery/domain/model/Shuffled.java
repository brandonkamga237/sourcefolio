package com.brandonkamga.sourcefolio.discovery.domain.model;

/** An entry with its place in the day's shuffle: a key that sorts the same way all day. */
public record Shuffled(CatalogEntry entry, String key) {
}
