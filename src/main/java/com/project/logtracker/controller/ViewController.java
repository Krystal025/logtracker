package com.project.logtracker.controller;

import com.project.logtracker.service.IssueService;
import com.project.logtracker.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final ProjectService projectService;
    private final IssueService issueService;

    @GetMapping({"/", "/projects"})
    public String dashboard(Model model) {
        model.addAttribute("projects", projectService.getAll());
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
