package com.project.logtracker.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "ISSUE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private Project project;

    @Column(name = "RAW_LOG", nullable = false, columnDefinition = "TEXT")
    private String rawLog;

    @Column(name = "TITLE", nullable = false, length = 1000)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private IssueStatus status;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true)
    private IssueAnalysis analysis;

    @OneToOne(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true)
    private IssueResolution resolution;

    public Issue(Project project, String rawLog, String title, IssueStatus status) {
        this.project = project;
        this.rawLog = rawLog;
        this.title = title;
        this.status = status;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void changeStatus(IssueStatus status) {
        this.status = status;
    }

    public void updateContent(String title, String rawLog) {
        this.title = title;
        this.rawLog = rawLog;
    }

    public void attachAnalysis(IssueAnalysis analysis) {
        this.analysis = analysis;
    }

    public void attachResolution(IssueResolution resolution) {
        this.resolution = resolution;
    }
}
