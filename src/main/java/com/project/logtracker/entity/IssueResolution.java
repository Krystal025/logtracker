package com.project.logtracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "ISSUE_RESOLUTION")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssueResolution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ISSUE_ID", nullable = false, unique = true)
    private Issue issue;

    @Column(name = "ACTUAL_CAUSE", nullable = false, length = 1000)
    private String actualCause;

    @Column(name = "ACTION_TAKEN", nullable = false, columnDefinition = "TEXT")
    private String actionTaken;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public IssueResolution(Issue issue, String actualCause, String actionTaken) {
        this.issue = issue;
        this.actualCause = actualCause;
        this.actionTaken = actionTaken;
    }

    public void update(String actualCause, String actionTaken) {
        this.actualCause = actualCause;
        this.actionTaken = actionTaken;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
