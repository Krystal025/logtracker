package com.project.logtracker.dto.analysis;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.logtracker.entity.IssueSeverity;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AnalysisResult(
        String summary,
        String rootCause,
        String recommendation,
        IssueSeverity severity,
        Double confidence,
        List<SimilarIssue> similarIssues
) {
    public static AnalysisResult withoutSimilar(
            String summary, String rootCause, String recommendation,
            IssueSeverity severity, Double confidence
    ) {
        return new AnalysisResult(summary, rootCause, recommendation, severity, confidence, null);
    }

    public AnalysisResult withSimilarIssues(List<SimilarIssue> similarIssues) {
        return new AnalysisResult(summary, rootCause, recommendation, severity, confidence, similarIssues);
    }
}
