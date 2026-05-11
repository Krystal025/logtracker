package com.project.logtracker.dto.issue;

import com.project.logtracker.dto.analysis.AnalysisResult;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IssueCreateRequest(
        @NotNull(message = "Project ID is required")
        Long projectId,

        @NotBlank(message = "Raw log is required")
        String rawLog,

        @NotBlank(message = "Issue title is required")
        @Size(max = 1000, message = "Title must be 1000 characters or fewer")
        String title,

        AnalysisResult analysis
) {
}
