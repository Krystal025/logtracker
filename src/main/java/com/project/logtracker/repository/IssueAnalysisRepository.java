package com.project.logtracker.repository;

import com.project.logtracker.entity.IssueAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IssueAnalysisRepository extends JpaRepository<IssueAnalysis, Long> {
    Optional<IssueAnalysis> findByIssueId(Long issueId);
}
