package com.project.logtracker.dto.issue;

import com.project.logtracker.dto.analysis.AnalysisResult;
import com.project.logtracker.entity.Issue;
import com.project.logtracker.entity.IssueAnalysis;
import com.project.logtracker.entity.IssueStatus;

import java.time.LocalDateTime;

public record IssueResponse(
        Long id,
        Long projectId,
        String title,
        String rawLog,
        AnalysisResult analysis,
        IssueResolutionResponse resolution,
        IssueStatus status,
        LocalDateTime createdAt
) {
    public static IssueResponse from(Issue issue) {
        return new IssueResponse(
                issue.getId(),
                issue.getProject().getId(),
                issue.getTitle(),
                issue.getRawLog(),
                toAnalysisResult(issue.getAnalysis()),
                IssueResolutionResponse.from(issue.getResolution()),
                issue.getStatus(),
                issue.getCreatedAt()
        );
    }

    private static AnalysisResult toAnalysisResult(IssueAnalysis analysis) {
        if (analysis == null) {
            return null;
        }
        return AnalysisResult.withoutSimilar(
                analysis.getSummary(),
                analysis.getRootCause(),
                analysis.getRecommendation(),
                analysis.getSeverity(),
                analysis.getConfidence()
        );
    }
}
