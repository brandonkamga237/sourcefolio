package com.brandonkamga.sourcefolio.showcase.domain.model;

import java.util.Objects;
import java.util.UUID;

public record ShowcaseId(UUID value) {

    public ShowcaseId {
        Objects.requireNonNull(value, "value");
    }

    public static ShowcaseId newId() {
        return new ShowcaseId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
