package com.project.logtracker.dto.issue;

import com.project.logtracker.entity.IssueStatus;
import jakarta.validation.constraints.NotNull;

public record IssueStatusUpdateRequest(
        @NotNull(message = "상태값은 필수입니다.")
        IssueStatus status
) {
}
