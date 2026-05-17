package com.project.logtracker.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectCreateRequest(
        @NotBlank(message = "Project name is required")
        @Size(max = 100, message = "Project name must be 100 characters or fewer")
        String name,

        @Size(max = 500, message = "Description must be 500 characters or fewer")
        String description
) {
}
