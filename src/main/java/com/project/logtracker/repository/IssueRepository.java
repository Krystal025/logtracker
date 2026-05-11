package com.project.logtracker.repository;

import com.project.logtracker.entity.Issue;
import com.project.logtracker.entity.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> {
    List<Issue> findAllByOrderByCreatedAtDesc();

    List<Issue> findByStatusOrderByCreatedAtDesc(IssueStatus status);

    List<Issue> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    List<Issue> findByProjectIdAndStatusOrderByCreatedAtDesc(Long projectId, IssueStatus status);
}
