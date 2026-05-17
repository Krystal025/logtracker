package com.project.logtracker.controller;

import com.project.logtracker.dto.issue.IssueResponse;
import com.project.logtracker.entity.IssueStatus;
import com.project.logtracker.service.IssueService;
import com.project.logtracker.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final ProjectService projectService;
    private final IssueService issueService;

    @GetMapping({"/", "/projects"})
    public String dashboard(Model model) {
        List<IssueResponse> allIssues = issueService.getAll(null, false);

        long unresolvedCount = allIssues.stream()
                .filter(i -> i.status() == IssueStatus.UNRESOLVED).count();
        long resolvedCount = allIssues.stream()
                .filter(i -> i.status() == IssueStatus.RESOLVED).count();

        Map<String, Long> severityCounts = allIssues.stream()
                .filter(i -> i.analysis() != null && i.analysis().severity() != null)
                .collect(Collectors.groupingBy(
                        i -> i.analysis().severity().name(),
                        Collectors.counting()
                ));

        Map<String, Long> orderedSeverity = new LinkedHashMap<>();
        for (String level : List.of("HIGH", "MEDIUM", "LOW")) {
            orderedSeverity.put(level, severityCounts.getOrDefault(level, 0L));
        }

        model.addAttribute("projects", projectService.getAll());
        model.addAttribute("unresolvedCount", unresolvedCount);
        model.addAttribute("resolvedCount", resolvedCount);
        model.addAttribute("severityCounts", orderedSeverity);
        return "dashboard";
    }

    @GetMapping("/projects/{projectId}")
    public String projectDetail(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "false") boolean unresolvedOnly,
            Model model
    ) {
        model.addAttribute("project", projectService.getById(projectId));
        model.addAttribute("issues", issueService.getAll(projectId, unresolvedOnly));
        model.addAttribute("unresolvedOnly", unresolvedOnly);
        return "project-detail";
    }

    @GetMapping("/issues/{issueId}")
    public String issueDetail(@PathVariable Long issueId, Model model) {
        model.addAttribute("issue", issueService.getById(issueId));
        return "issue-detail";
    }
}
