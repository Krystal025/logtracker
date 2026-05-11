package com.project.logtracker.controller;

import com.project.logtracker.dto.analysis.AnalysisRequest;
import com.project.logtracker.dto.analysis.AnalysisResult;
import com.project.logtracker.dto.issue.IssueResponse;
import com.project.logtracker.service.AnalysisService;
import com.project.logtracker.service.IssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;
    private final IssueService issueService;

    @PostMapping
    public AnalysisResult analyze(@Valid @RequestBody AnalysisRequest request) {
        return analysisService.analyze(request.rawLog());
    }

    @PostMapping("/issues/{issueId}")
    public IssueResponse analyzeIssue(@PathVariable Long issueId) {
        AnalysisResult analysisResult = analysisService.analyze(issueService.getRawLog(issueId));
        return issueService.saveAnalysis(issueId, analysisResult);
    }
}
