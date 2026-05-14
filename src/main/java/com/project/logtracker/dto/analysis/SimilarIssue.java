package com.project.logtracker.dto.analysis;

public record SimilarIssue(
        Long issueId,
        String title,
        String rootCause,
        String actualCause,
        String actionTaken
) {
}
