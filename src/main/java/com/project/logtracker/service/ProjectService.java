package com.project.logtracker.service;

import com.project.logtracker.dto.project.ProjectCreateRequest;
import com.project.logtracker.dto.project.ProjectResponse;
import com.project.logtracker.entity.Project;
import com.project.logtracker.exception.ResourceNotFoundException;
import com.project.logtracker.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Transactional
    public ProjectResponse create(ProjectCreateRequest request) {
        Project project = new Project(request.name());
        Project savedProject = projectRepository.save(project);
        return ProjectResponse.from(savedProject);
    }

    public List<ProjectResponse> getAll() {
        return projectRepository.findAll().stream()
                .map(ProjectResponse::from)
                .toList();
    }

    public ProjectResponse getById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
        return ProjectResponse.from(project);
    }
}
