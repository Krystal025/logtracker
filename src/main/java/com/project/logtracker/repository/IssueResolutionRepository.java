package com.project.logtracker.repository;

import com.project.logtracker.entity.IssueResolution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IssueResolutionRepository extends JpaRepository<IssueResolution, Long> {
    Optional<IssueResolution> findByIssueId(Long issueId);
}
