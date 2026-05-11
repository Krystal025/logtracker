package com.project.logtracker.service;

import com.project.logtracker.dto.analysis.AnalysisResult;
import com.project.logtracker.dto.issue.IssueCreateRequest;
import com.project.logtracker.dto.issue.IssueResponse;
import com.project.logtracker.dto.issue.IssueUpdateRequest;
import com.project.logtracker.entity.Issue;
import com.project.logtracker.entity.IssueAnalysis;
import com.project.logtracker.entity.IssueResolution;
import com.project.logtracker.entity.IssueStatus;
import com.project.logtracker.entity.Project;
import com.project.logtracker.exception.ResourceNotFoundException;
import com.project.logtracker.repository.IssueAnalysisRepository;
import com.project.logtracker.repository.IssueRepository;
import com.project.logtracker.repository.IssueResolutionRepository;
import com.project.logtracker.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IssueService {

    private final IssueRepository issueRepository;
    private final IssueAnalysisRepository issueAnalysisRepository;
    private final IssueResolutionRepository issueResolutionRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public IssueResponse create(IssueCreateRequest request) {
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + request.projectId()));

        Issue issue = new Issue(project, request.rawLog(), request.title(), IssueStatus.UNRESOLVED);
        Issue savedIssue = issueRepository.save(issue);

        AnalysisResult analysisResult = request.analysis();
        if (analysisResult != null) {
            upsertAnalysis(savedIssue, analysisResult);
        }

        return IssueResponse.from(savedIssue);
    }

    public List<IssueResponse> getAll(Long projectId, boolean unresolvedOnly) {
        List<Issue> issues;
        if (projectId == null) {
            issues = unresolvedOnly
                    ? issueRepository.findByStatusOrderByCreatedAtDesc(IssueStatus.UNRESOLVED)
                    : issueRepository.findAllByOrderByCreatedAtDesc();
        } else {
            issues = unresolvedOnly
                    ? issueRepository.findByProjectIdAndStatusOrderByCreatedAtDesc(projectId, IssueStatus.UNRESOLVED)
                    : issueRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
        }

        return issues.stream()
                .map(IssueResponse::from)
                .toList();
    }

    public IssueResponse getById(Long id) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + id));
        return IssueResponse.from(issue);
    }

    public String getRawLog(Long id) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + id));
        return issue.getRawLog();
    }

    @Transactional
    public IssueResponse saveAnalysis(Long issueId, AnalysisResult analysisResult) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));
        upsertAnalysis(issue, analysisResult);
        return IssueResponse.from(issue);
    }

    @Transactional
    public IssueResponse updateStatus(Long id, IssueStatus status) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + id));
        issue.changeStatus(status);
        return IssueResponse.from(issue);
    }

    @Transactional
    public IssueResponse updateContent(Long id, IssueUpdateRequest request) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + id));
        issue.updateContent(request.title(), request.rawLog());
        return IssueResponse.from(issue);
    }

    @Transactional
    public IssueResponse createResolution(Long issueId, String actualCause, String actionTaken) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));
        IssueResolution resolution = issueResolutionRepository.findByIssueId(issueId)
                .map(existingResolution -> {
                    existingResolution.update(actualCause, actionTaken);
                    return existingResolution;
                })
                .orElseGet(() -> new IssueResolution(issue, actualCause, actionTaken));

        issueResolutionRepository.save(resolution);
        issue.attachResolution(resolution);
        issue.changeStatus(IssueStatus.RESOLVED);
        return IssueResponse.from(issue);
    }

    private void upsertAnalysis(Issue issue, AnalysisResult analysisResult) {
        double confidence = analysisResult.confidence() == null ? 0.0 : analysisResult.confidence();
        IssueAnalysis issueAnalysis = issueAnalysisRepository.findByIssueId(issue.getId())
                .map(existingAnalysis -> {
                    existingAnalysis.update(
                            analysisResult.summary(),
                            analysisResult.rootCause(),
                            analysisResult.recommendation(),
                            confidence
                    );
                    return existingAnalysis;
                })
                .orElseGet(() -> new IssueAnalysis(
                        issue,
                        analysisResult.summary(),
                        analysisResult.rootCause(),
                        analysisResult.recommendation(),
                        confidence
                ));

        issueAnalysisRepository.save(issueAnalysis);
        issue.attachAnalysis(issueAnalysis);
    }
}
