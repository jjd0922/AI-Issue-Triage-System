package com.example.aiissuetriage.issue.infrastructure.persistence;

import com.example.aiissuetriage.issue.domain.IssueCategory;
import com.example.aiissuetriage.issue.domain.IssuePriority;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "issue_analysis")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssueAnalysisEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long issueId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private IssueCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private IssuePriority priority;

    @Column(nullable = false, length = 1000)
    private String summary;

    @Column(nullable = false, length = 2000)
    private String recommendation;

    @Column(nullable = false)
    private double confidence;

    @Column(nullable = false, length = 100)
    private String modelName;

    @Column(columnDefinition = "TEXT")
    private String rawResponse;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    IssueAnalysisEntity(
            Long id,
            Long issueId,
            IssueCategory category,
            IssuePriority priority,
            String summary,
            String recommendation,
            double confidence,
            String modelName,
            String rawResponse,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.issueId = issueId;
        this.category = category;
        this.priority = priority;
        this.summary = summary;
        this.recommendation = recommendation;
        this.confidence = confidence;
        this.modelName = modelName;
        this.rawResponse = rawResponse;
        this.createdAt = createdAt;
    }
}
