package com.project.logtracker.dto.issue;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IssueResolutionCreateRequest(
        @NotBlank(message = "원인 작성은 필수입니다.")
        @Size(max = 1000, message = "원인은 1000자 이하여야 합니다.")
        String actualCause,

        @NotBlank(message = "조치 내용 작성은 필수입니다.")
        String actionTaken
) {
}
