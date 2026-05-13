package com.project.logtracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "ISSUE_ANALYSIS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssueAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ISSUE_ID", nullable = false, unique = true)
    private Issue issue;

    @Column(name = "SUMMARY", nullable = false, length = 1000)
    private String summary;

    @Column(name = "ROOT_CAUSE", nullable = false, length = 1000)
    private String rootCause;

    @Column(name = "RECOMMENDATION", nullable = false, length = 1000)
    private String recommendation;

    @Enumerated(EnumType.STRING)
    @Column(name = "SEVERITY", nullable = false, length = 20)
    private IssueSeverity severity;

    @Column(name = "CONFIDENCE", nullable = false)
    private Double confidence;

    public IssueAnalysis(
            Issue issue,
            String summary,
            String rootCause,
            String recommendation,
            IssueSeverity severity,
            Double confidence
    ) {
        this.issue = issue;
        this.summary = summary;
        this.rootCause = rootCause;
        this.recommendation = recommendation;
        this.severity = severity;
        this.confidence = confidence;
    }

    public void update(String summary, String rootCause, String recommendation, IssueSeverity severity, Double confidence) {
        this.summary = summary;
        this.rootCause = rootCause;
        this.recommendation = recommendation;
        this.severity = severity;
        this.confidence = confidence;
    }
}
