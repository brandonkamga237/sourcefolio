package com.brandonkamga.sourcefolio.profile.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CountryAnswer(@NotBlank String country) {
}
