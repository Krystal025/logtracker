package com.project.logtracker.dto.issue;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IssueUpdateRequest(
        @NotBlank(message = "Issue title is required")
        @Size(max = 1000, message = "Title must be 1000 characters or fewer")
        String title,

        @NotBlank(message = "Raw log is required")
        String rawLog
) {
}
