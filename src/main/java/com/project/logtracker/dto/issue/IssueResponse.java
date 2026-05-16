package com.project.logtracker.dto.issue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.logtracker.dto.analysis.AnalysisResult;
import com.project.logtracker.dto.analysis.SimilarIssue;
import com.project.logtracker.entity.Issue;
import com.project.logtracker.entity.IssueAnalysis;
import com.project.logtracker.entity.IssueStatus;

import java.time.LocalDateTime;
import java.util.List;

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
    private static final ObjectMapper MAPPER = new ObjectMapper();

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
        ).withSimilarIssues(parseSimilarIssues(analysis.getSimilarIssues()));
    }

    private static List<SimilarIssue> parseSimilarIssues(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, new TypeReference<List<SimilarIssue>>() {});
        } catch (Exception e) {
            return null;
        }
    }
}
