package com.project.logtracker.controller;

import com.project.logtracker.dto.issue.IssueCreateRequest;
import com.project.logtracker.dto.issue.IssueResolutionCreateRequest;
import com.project.logtracker.dto.issue.IssueResponse;
import com.project.logtracker.dto.issue.IssueStatusUpdateRequest;
import com.project.logtracker.dto.issue.IssueUpdateRequest;
import com.project.logtracker.service.IssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/issues")
public class IssueController {

    private final IssueService issueService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IssueResponse create(@Valid @RequestBody IssueCreateRequest request) {
        return issueService.create(request);
    }

    @GetMapping
    public List<IssueResponse> getAll(
            @RequestParam(required = false) Long projectId,
            @RequestParam(defaultValue = "false") boolean unresolvedOnly
    ) {
        return issueService.getAll(projectId, unresolvedOnly);
    }

    @GetMapping("/{id}")
    public IssueResponse getById(@PathVariable Long id) {
        return issueService.getById(id);
    }

    @PatchMapping("/{id}/status")
    public IssueResponse updateStatus(@PathVariable Long id, @Valid @RequestBody IssueStatusUpdateRequest request) {
        return issueService.updateStatus(id, request.status());
    }

    @PatchMapping("/{id}")
    public IssueResponse updateContent(@PathVariable Long id, @Valid @RequestBody IssueUpdateRequest request) {
        return issueService.updateContent(id, request);
    }

    @PostMapping("/{id}/resolution")
    @ResponseStatus(HttpStatus.CREATED)
    public IssueResponse createResolution(@PathVariable Long id, @Valid @RequestBody IssueResolutionCreateRequest request) {
        return issueService.createResolution(id, request.actualCause(), request.actionTaken());
    }
}
