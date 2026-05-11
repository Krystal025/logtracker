package com.project.logtracker.dto.analysis;

import jakarta.validation.constraints.NotBlank;

public record AnalysisRequest(
        @NotBlank(message = "로그 내용은 필수입니다.")
        String rawLog
) {
}
