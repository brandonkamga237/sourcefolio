package com.brandonkamga.sourcefolio.discovery.api.dto;

import com.brandonkamga.sourcefolio.discovery.domain.model.ProjectCount;

public record ProjectCountResponse(String repository, long contributors) {

    public static ProjectCountResponse of(ProjectCount project) {
        return new ProjectCountResponse(project.repository(), project.contributors());
    }
}
