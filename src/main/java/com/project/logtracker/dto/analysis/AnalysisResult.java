package com.project.logtracker.dto.analysis;

import com.project.logtracker.entity.IssueSeverity;

public record AnalysisResult(
        String summary,
        String rootCause,
        String recommendation,
        IssueSeverity severity,
        Double confidence
) {
}
