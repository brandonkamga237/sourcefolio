package com.brandonkamga.sourcefolio.contribution.domain.model;

/** Bytes of code in one language, with the colour GitHub gives it (may be null). */
public record LanguageBytes(String name, String color, long bytes) {
}
