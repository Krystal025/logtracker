package com.project.logtracker.dto.analysis;

public record AnalysisResult(
        String summary,
        String rootCause,
        String recommendation,
        Double confidence
) {
}
