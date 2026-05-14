package com.project.logtracker.dto.analysis;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnalysisRequest(
        @NotNull(message = "Project ID는 필수입니다.")
        Long projectId,

        @NotBlank(message = "로그 내용은 필수입니다.")
        String rawLog
) {
}
