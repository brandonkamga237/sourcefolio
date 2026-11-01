package com.brandonkamga.sourcefolio.profile.api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Slugs, most important first; the use case checks one to three, known and distinct. */
public record SpecialtiesAnswer(@NotNull List<String> specialties) {
}
