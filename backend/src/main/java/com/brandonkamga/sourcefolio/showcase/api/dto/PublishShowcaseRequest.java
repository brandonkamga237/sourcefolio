package com.brandonkamga.sourcefolio.showcase.api.dto;

import com.brandonkamga.sourcefolio.shared.exception.BadRequestException;
import com.brandonkamga.sourcefolio.showcase.application.PublishShowcase;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

/** The candidate to publish, by its repository as the candidates list gives it ("owner/name"). */
public record PublishShowcaseRequest(@NotBlank String repository) {

    public PublishShowcase toCommand(UUID memberId) {
        return new PublishShowcase(memberId, RepositoryCoordinates.parse(repository)
                .orElseThrow(() -> new BadRequestException("showcase.repository-invalid")));
    }
}
