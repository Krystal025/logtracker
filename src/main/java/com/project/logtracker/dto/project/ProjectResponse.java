package com.project.logtracker.dto.project;

import com.project.logtracker.entity.Project;

import java.time.LocalDate;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        LocalDate createdAt
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedAt().toLocalDate()
        );
    }
}
