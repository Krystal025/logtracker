package com.project.logtracker.dto.issue;

import com.project.logtracker.entity.IssueResolution;

import java.time.LocalDateTime;

public record IssueResolutionResponse(
        Long id,
        String actualCause,
        String actionTaken,
        LocalDateTime createdAt
) {
    public static IssueResolutionResponse from(IssueResolution resolution) {
        if (resolution == null) {
            return null;
        }
        return new IssueResolutionResponse(
                resolution.getId(),
                resolution.getActualCause(),
                resolution.getActionTaken(),
                resolution.getCreatedAt()
        );
    }
}
